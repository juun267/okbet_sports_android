package org.cxct.sportlottery.ui.game.v3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_game_outright.*
import kotlinx.android.synthetic.main.fragment_game_outright.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.CateMenuCode
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.menu.OddsType


class GameOutrightFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    private val args: GameOutrightFragmentArgs by navArgs()

    private val outrightOddAdapter by lazy {
        OutrightOddAdapter().apply {
            outrightOddListener = OutrightOddListener {
                viewModel.updateOutrightOddsSelectedState(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_outright, container, false).apply {
            setupGameFilterRow(this)
            setupOutrightOddList(this)
        }
    }

    private fun setupGameFilterRow(view: View) {
        view.outright_filter_row.apply {

            isSearchViewVisible = false

            backClickListener = View.OnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupOutrightOddList(view: View) {
        view.outright_league_odd_list.apply {

            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            adapter = outrightOddAdapter

            addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            initObserve()
            initSocketReceiver()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStart() {
        super.onStart()

        service.subscribeHallChannel(args.sportType.code, CateMenuCode.OUTRIGHT.code, args.eventId)

        viewModel.getOutrightOddsList(args.eventId)
        loading()
    }

    private fun initObserve() {
        viewModel.outrightOddsListResult.observe(this.viewLifecycleOwner, Observer {
            hideLoading()

            it.getContentIfNotHandled()?.let { outrightOddsListResult ->
                if (outrightOddsListResult.success) {
                    val matchOdd =
                        outrightOddsListResult.outrightOddsListData?.leagueOdds?.get(0)?.matchOdds?.get(
                            0
                        )

                    outright_filter_row.sportName =
                        outrightOddsListResult.outrightOddsListData?.sport?.name ?: ""

                    outright_league_name.text =
                        outrightOddsListResult.outrightOddsListData?.leagueOdds?.get(0)?.league?.name
                            ?: ""

                    outright_league_date.text = matchOdd?.startDate ?: ""

                    outright_league_time.text = matchOdd?.startTime ?: ""

                    outrightOddAdapter.data = matchOdd?.displayList ?: listOf()
                }
            }
        })

        viewModel.betInfoList.observe(this.viewLifecycleOwner, Observer {
            val odds = outrightOddAdapter.data.filterIsInstance<Odd>()

            odds.forEach { odd ->
                odd.isSelected = it.any {
                    it.matchOdd.oddsId == odd.id
                }
            }

            outrightOddAdapter.notifyDataSetChanged()
        })

        viewModel.oddsType.observe(this.viewLifecycleOwner, Observer {
            val oddsType = when (it) {
                OddsType.EU.value -> OddsType.EU
                OddsType.HK.value -> OddsType.HK
                else -> null
            }

            oddsType?.let {
                outrightOddAdapter.oddsType = oddsType
            }
        })
    }

    private fun initSocketReceiver() {
        receiver.oddsChange.observe(this.viewLifecycleOwner, Observer {
            it?.let { oddsChangeEvent ->
                oddsChangeEvent.odds?.let { oddTypeSocketMap ->

                    val odds = outrightOddAdapter.data.filterIsInstance<Odd>()
                    val oddsType = outrightOddAdapter.oddsType

                    odds.forEach { odd ->
                        oddTypeSocketMap.forEach { oddTypeSocketMapEntry ->

                            val oddSocket = oddTypeSocketMapEntry.value.find { oddSocket ->
                                oddSocket?.id == odd.id
                            }

                            oddSocket?.let { oddSocketNonNull ->

                                when (oddsType) {
                                    OddsType.EU -> {
                                        odd.odds?.let { oddValue ->
                                            oddSocketNonNull.odds?.let { oddSocketValue ->
                                                when {
                                                    oddValue > oddSocketValue -> {
                                                        odd.oddState =
                                                            OddState.SMALLER.state
                                                    }
                                                    oddValue < oddSocketValue -> {
                                                        odd.oddState =
                                                            OddState.LARGER.state
                                                    }
                                                    oddValue == oddSocketValue -> {
                                                        odd.oddState =
                                                            OddState.SAME.state
                                                    }
                                                }

                                            }
                                        }

                                        odd.odds = oddSocketNonNull.odds
                                    }

                                    OddsType.HK -> {
                                        odd.hkOdds?.let { oddValue ->
                                            oddSocketNonNull.hkOdds?.let { oddSocketValue ->
                                                when {
                                                    oddValue > oddSocketValue -> {
                                                        odd.oddState =
                                                            OddState.SMALLER.state
                                                    }
                                                    oddValue < oddSocketValue -> {
                                                        odd.oddState =
                                                            OddState.LARGER.state
                                                    }
                                                    oddValue == oddSocketValue -> {
                                                        odd.oddState =
                                                            OddState.SAME.state
                                                    }
                                                }

                                            }
                                        }

                                        odd.hkOdds = oddSocketNonNull.hkOdds
                                    }
                                }

                                odd.status = oddSocketNonNull.status

                                outrightOddAdapter.notifyItemChanged(odds.indexOf(odd))
                            }
                        }
                    }
                }
            }
        })

        receiver.globalStop.observe(this.viewLifecycleOwner, Observer {
            it?.let { globalStopEvent ->

                val odds = outrightOddAdapter.data.filterIsInstance<Odd>()

                odds.forEach { odd ->
                    when (globalStopEvent.producerId) {
                        null -> {
                            odd.status = BetStatus.DEACTIVATED.code
                        }
                        else -> {
                            odd.producerId?.let { producerId ->
                                if (producerId == globalStopEvent.producerId) {
                                    odd.status = BetStatus.DEACTIVATED.code
                                }
                            }
                        }
                    }

                    outrightOddAdapter.notifyItemChanged(odds.indexOf(odd))
                }
            }
        })

        receiver.producerUp.observe(this.viewLifecycleOwner, Observer {
            it?.let { _ ->
                service.unsubscribeAllHallChannel()
                service.subscribeHallChannel(
                    args.sportType.code,
                    CateMenuCode.OUTRIGHT.code,
                    args.eventId
                )
            }
        })
    }

    override fun onStop() {
        super.onStop()

        service.unsubscribeAllHallChannel()
    }
}