package org.cxct.sportlottery.ui.game.outright

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
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
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.GameConfigManager


class GameOutrightFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    private val args: GameOutrightFragmentArgs by navArgs()

    private val outrightLeagueOddAdapter by lazy {
        OutrightLeagueOddAdapter().apply {
            outrightOddListener = OutrightOddListener(
                { matchOdd, odd ->
                    matchOdd?.let {
                        addOddsDialog(matchOdd, odd)
                    }
                },
                { oddsKey, matchOdd ->
                    val action =
                        GameOutrightFragmentDirections.actionGameOutrightFragmentToGameOutrightMoreFragment(
                            oddsKey,
                            matchOdd.apply {
                                this.matchInfo?.gameType = args.gameType.key
                            }
                        )
                    findNavController().navigate(action)
                },
                { matchOdd, oddsKey ->
                    this.data.find { it == matchOdd }?.odds?.get(oddsKey)?.forEach { odd ->
                        odd?.isExpand?.let { isExpand ->
                            odd.isExpand = !isExpand
                        }
                    }
                    this.notifyItemChanged(this.data.indexOf(matchOdd))
                }
            )
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

            adapter = outrightLeagueOddAdapter

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            initObserve()
            initSocketObserver()
            initView()

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

                    game_toolbar_match_type.text = GameType.values()
                        .find { gameType -> gameType.key == args.gameType.key }?.string?.let { stringId ->
                            getString(
                                stringId
                            )
                        }

                    GameConfigManager.getTitleBarBackground(outrightOddsListResult.outrightOddsListData?.sport?.code)
                        ?.let { gameImg ->
                            game_toolbar_bg.setBackgroundResource(gameImg)
                        }

                    val outrightLeagueOddDataList =
                        outrightOddsListResult.outrightOddsListData?.leagueOdds?.firstOrNull()?.matchOdds
                            ?: listOf()
                    outrightLeagueOddDataList.forEach { matchOdd ->
                        val firstKey = matchOdd?.odds?.keys?.firstOrNull()
                        matchOdd?.odds?.forEach {
                            if (it.key == firstKey) {
                                it.value.filterNotNull().forEach { odd ->
                                    odd.isExpand = true
                                }
                            }
                        }
                    }

                    outrightLeagueOddAdapter.data = outrightLeagueOddDataList

                    subscribeChannelHall(
                        args.gameType.key,
                        PlayCate.OUTRIGHT.value,
                        args.eventId
                    )
                }
            }
        })

        viewModel.betInfoList.observe(this.viewLifecycleOwner, {
            it.peekContent().let {
                val odds = mutableListOf<Odd>()

                outrightLeagueOddAdapter.data.forEach { matchOdd ->
                    matchOdd?.odds?.values?.forEach { oddList ->
                        odds.addAll(oddList.filterNotNull())
                    }
                }

                odds.forEach { odd ->
                    odd.isSelected = it.any { betInfoListData ->
                        betInfoListData.matchOdd.oddsId == odd.id
                    }
                }

                outrightLeagueOddAdapter.notifyDataSetChanged()
            }
        })

        viewModel.oddsType.observe(this.viewLifecycleOwner, {
            it?.let { oddsType ->
                outrightLeagueOddAdapter.oddsType = oddsType
            }
        })
    }

    private fun initSocketObserver() {

        receiver.oddsChange.observe(this.viewLifecycleOwner, {
            it?.let { oddsChangeEvent ->
                oddsChangeEvent.updateOddsSelectedState()
                oddsChangeEvent.odds?.let { oddTypeSocketMap ->
                    val odds = mutableListOf<Odd>()

                    outrightLeagueOddAdapter.data.forEach { matchOdd ->
                        matchOdd?.odds?.values?.forEach { oddList ->
                            odds.addAll(oddList.filterNotNull())
                        }
                    }

                    odds.forEachIndexed { index: Int, odd: Odd ->
                        val oddsType = outrightLeagueOddAdapter.oddsType

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

                                outrightLeagueOddAdapter.notifyItemChanged(index)
                            }
                        }

                    }
                }
            }
        })

        receiver.globalStop.observe(this.viewLifecycleOwner, {
            it?.let { globalStopEvent ->
                val odds = mutableListOf<Odd>()

                outrightLeagueOddAdapter.data.forEach { matchOdd ->
                    matchOdd?.odds?.values?.forEach { oddList ->
                        odds.addAll(oddList.filterNotNull())
                    }
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

                    outrightLeagueOddAdapter.notifyItemChanged(odds.indexOf(odd))
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

    private fun initView() {
        game_toolbar_sport_type.text = getString(R.string.outright_row_entrance)
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