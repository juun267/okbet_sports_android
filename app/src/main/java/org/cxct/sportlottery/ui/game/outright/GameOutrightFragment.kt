package org.cxct.sportlottery.ui.game.outright

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_game_outright.*
import kotlinx.android.synthetic.main.fragment_game_outright.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.CateMenuCode
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.v3.OutrightOddAdapter
import org.cxct.sportlottery.ui.game.v3.OutrightOddListener
import org.cxct.sportlottery.ui.menu.OddsType


class GameOutrightFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    private val args: GameOutrightFragmentArgs by navArgs()

    private val outrightOddAdapter by lazy {
        OutrightOddAdapter().apply {
            outrightOddListener = OutrightOddListener { matchOdd, odd ->
                matchOdd?.let {
                    addOddsDialog(matchOdd, odd)
                }
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
                OutrightOddDividerDecoration(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.divider_gray
                    )
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

                    outrightOddAdapter.matchOdd = matchOdd
                }
            }
        })

        viewModel.betInfoList.observe(this.viewLifecycleOwner, Observer {
            val odds = outrightOddAdapter.matchOdd?.displayList?.filterIsInstance<Odd>()

            odds?.forEach { odd ->
                odd.isSelected = it.any {
                    it.matchOdd.oddsId == odd.id
                }
            }

            outrightOddAdapter.notifyDataSetChanged()
        })

        viewModel.oddsType.observe(this.viewLifecycleOwner, Observer {
            it?.let { oddsType ->
                outrightOddAdapter.oddsType = oddsType
            }
        })
    }

    private fun initSocketReceiver() {
        receiver.oddsChange.observe(this.viewLifecycleOwner, Observer {
            it?.let { oddsChangeEvent ->
                oddsChangeEvent.odds?.let { oddTypeSocketMap ->

                    outrightOddAdapter.matchOdd?.displayList?.forEachIndexed { index: Int, odd: Any ->
                        if (odd is Odd) {
                            val oddsType = outrightOddAdapter.oddsType

                            oddTypeSocketMap.forEach { oddTypeSocketMapEntry ->
                                oddTypeSocketMapEntry.value.onEach { odd ->
                                    odd?.isSelected =
                                        viewModel.betInfoRepository.betInfoList.value?.any { betInfoListData ->
                                            betInfoListData.matchOdd.oddsId == odd?.id
                                        }
                                }

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
                                        }
                                    }

                                    odd.odds = oddSocketNonNull.odds
                                    odd.hkOdds = oddSocketNonNull.hkOdds

                                    odd.status = oddSocketNonNull.status

                                    outrightOddAdapter.notifyItemChanged(index)
                                }
                            }

                        }
                    }

                }
            }
        })

        receiver.globalStop.observe(this.viewLifecycleOwner, Observer {
            it?.let { globalStopEvent ->

                val odds = outrightOddAdapter.matchOdd?.displayList?.filterIsInstance<Odd>()

                odds?.forEach { odd ->
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

    private fun addOddsDialog(
        matchOdd: MatchOdd,
        odd: Odd
    ) {
        viewModel.updateMatchBetList(
            MatchType.OUTRIGHT,
            args.sportType,
            matchOdd,
            odd
        )
    }

    override fun onStop() {
        super.onStop()

        service.unsubscribeAllHallChannel()
    }
}