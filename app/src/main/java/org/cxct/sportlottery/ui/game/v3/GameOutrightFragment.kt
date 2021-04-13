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


class GameOutrightFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    private val args: GameOutrightFragmentArgs by navArgs()

    private val outrightOddAdapter by lazy {
        OutrightOddAdapter().apply {
            outrightOddListener = OutrightOddListener {
                viewModel.updateOutrightOddsSelectedState(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        service.subscribeHallChannel(args.sportType.code, CateMenuCode.OUTRIGHT.code, args.eventId)
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

            viewModel.getOutrightOddsList(args.eventId)
            loading()

        } catch (e: Exception) {
            e.printStackTrace()
        }
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
    }

    private fun initSocketReceiver() {
        receiver.oddsChange.observe(this.viewLifecycleOwner, Observer {
            it?.let { oddsChangeEvent ->
                oddsChangeEvent.odds?.let { oddTypeSocketMap ->

                    val odds = outrightOddAdapter.data.filterIsInstance<Odd>()

                    odds.forEach { odd ->
                        oddTypeSocketMap.forEach { oddTypeSocketMapEntry ->

                            val oddSocket = oddTypeSocketMapEntry.value.find { oddSocket ->
                                oddSocket.id == odd.id
                            }

                            oddSocket?.let { oddSocketNonNull ->
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

    override fun onDestroy() {
        super.onDestroy()

        service.unsubscribeAllHallChannel()
    }
}