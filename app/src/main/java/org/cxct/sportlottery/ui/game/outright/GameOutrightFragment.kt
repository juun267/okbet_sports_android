package org.cxct.sportlottery.ui.game.outright

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_game_outright.*
import kotlinx.android.synthetic.main.fragment_game_outright.view.*
import kotlinx.android.synthetic.main.view_game_toolbar_v4.*
import kotlinx.android.synthetic.main.view_game_toolbar_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.GameConfigManager


class GameOutrightFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    private val args: GameOutrightFragmentArgs by navArgs()

    private val gameToolbarMatchTypeText = { matchType: MatchType? ->
        when (matchType) {
            MatchType.IN_PLAY -> getString(R.string.home_tab_in_play)
            MatchType.TODAY -> getString(R.string.home_tab_today)
            MatchType.EARLY -> getString(R.string.home_tab_early)
            MatchType.PARLAY -> getString(R.string.home_tab_parlay)
            MatchType.AT_START -> getString(R.string.home_tab_at_start)
            MatchType.OUTRIGHT -> getString(R.string.home_tab_outright)
            else -> ""
        }
    }

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
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_game_outright, container, false).apply {
            setupGameFilterRow(this)
            setupOutrightOddList(this)
        }
    }

    private fun setupGameFilterRow(view: View) {
        view.outright_toolbar.game_toolbar_back.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupOutrightOddList(view: View) {
        view.outright_league_odd_list.apply {

            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            adapter = outrightOddAdapter

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            initObserve()
            initSocketObserver()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.getOutrightOddsList(args.eventId)
        loading()
    }

    private fun initObserve() {
        viewModel.outrightOddsListResult.observe(this.viewLifecycleOwner, {
            hideLoading()

            it.getContentIfNotHandled()?.let { outrightOddsListResult ->
                if (outrightOddsListResult.success) {

                    val matchOdd = outrightOddsListResult.outrightOddsListData?.leagueOdds?.firstOrNull()?.matchOdds?.firstOrNull()

                    game_toolbar_sport_type.text = GameType.values().find { gameType -> gameType.key == args.gameType.key }?.string?.let { stringId -> getString(stringId) }

                    GameConfigManager.getTitleBarBackground(outrightOddsListResult.outrightOddsListData?.sport?.code)?.let { gameImg ->
                        game_toolbar_bg.setBackgroundResource(gameImg)
                    }

                    outright_league_name.text = outrightOddsListResult.outrightOddsListData?.leagueOdds?.firstOrNull()?.league?.name?: ""

                    outright_league_date.text = matchOdd?.startDate ?: ""

                    outright_league_time.text = matchOdd?.startTime ?: ""

                    outrightOddAdapter.matchOdd = matchOdd

                    subscribeChannelHall(
                        args.gameType.key,
                        PlayCate.OUTRIGHT.value,
                        args.eventId
                    )
                }
            }
        })

        viewModel.curMatchType.observe(viewLifecycleOwner, {
                game_toolbar_match_type.text = gameToolbarMatchTypeText(it)
        })

        viewModel.curChildMatchType.observe(viewLifecycleOwner, {
                game_toolbar_match_type.text = gameToolbarMatchTypeText(it)
        })

        viewModel.betInfoList.observe(this.viewLifecycleOwner, {
            it.peekContent().let {
                val odds = mutableListOf<Odd>()
                outrightOddAdapter.matchOdd?.odds?.values?.forEach { oddList ->
                    odds.addAll(oddList.filterNotNull())
                }

                odds.forEach { odd ->
                    odd.isSelected = it.any { betInfoListData ->
                        betInfoListData.matchOdd.oddsId == odd.id
                    }
                }

                outrightOddAdapter.notifyDataSetChanged()
            }
        })

        viewModel.oddsType.observe(this.viewLifecycleOwner, {
            it?.let { oddsType ->
                outrightOddAdapter.oddsType = oddsType
            }
        })
    }

    private fun initSocketObserver() {

        receiver.oddsChange.observe(this.viewLifecycleOwner, {
            it?.let { oddsChangeEvent ->
                oddsChangeEvent.updateOddsSelectedState()
                oddsChangeEvent.odds?.let { oddTypeSocketMap ->
                    val odds = mutableListOf<Odd>()
                    outrightOddAdapter.matchOdd?.odds?.values?.forEach { oddList ->
                        odds.addAll(oddList.filterNotNull())
                    }

                    odds.forEachIndexed { index: Int, odd: Odd ->
                        val oddsType = outrightOddAdapter.oddsType

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
                                                        odd.oddState = OddState.SMALLER.state
                                                    }
                                                    oddValue < oddSocketValue -> {
                                                        odd.oddState = OddState.LARGER.state
                                                    }
                                                    oddValue == oddSocketValue -> {
                                                        odd.oddState = OddState.SAME.state
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
                                                        odd.oddState = OddState.SMALLER.state
                                                    }
                                                    oddValue < oddSocketValue -> {
                                                        odd.oddState = OddState.LARGER.state
                                                    }
                                                    oddValue == oddSocketValue -> {
                                                        odd.oddState = OddState.SAME.state
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
        })

        receiver.globalStop.observe(this.viewLifecycleOwner, {
            it?.let { globalStopEvent ->
                val odds = mutableListOf<Odd>()
                outrightOddAdapter.matchOdd?.odds?.values?.forEach { oddList ->
                    odds.addAll(oddList.filterNotNull())
                }

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

        receiver.producerUp.observe(this.viewLifecycleOwner, {
            unSubscribeChannelHallAll()
            subscribeChannelHall(
                args.gameType.key,
                PlayCate.OUTRIGHT.value,
                args.eventId
            )
        })
    }

    private fun OddsChangeEvent.updateOddsSelectedState(): OddsChangeEvent {
        this.odds?.let { oddTypeSocketMap ->
            oddTypeSocketMap.mapValues { oddTypeSocketMapEntry ->
                oddTypeSocketMapEntry.value.onEach { odd ->
                    odd?.isSelected =
                        viewModel.betInfoList.value?.peekContent()?.any { betInfoListData ->
                            betInfoListData.matchOdd.oddsId == odd?.id
                        }
                }
            }
        }

        return this
    }

    private fun addOddsDialog(
        matchOdd: MatchOdd,
        odd: Odd,
    ) {
        viewModel.updateMatchBetListForOutRight(
            matchType = MatchType.OUTRIGHT,
            gameType = args.gameType,
            matchOdd = matchOdd,
            odd = odd
        )
    }

    override fun onStop() {
        super.onStop()

        unSubscribeChannelHallAll()
    }
}