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
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.GameConfigManager
import org.cxct.sportlottery.util.SocketUpdateUtil


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

                    subscribeChannelHall(matchOdd)

                    this.data.find { it == matchOdd }?.oddsMap?.get(oddsKey)?.forEach { odd ->
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

        viewModel.getOutrightOddsList(args.gameType, args.eventId)
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

                    outrightLeagueOddDataList.forEachIndexed { index, matchOdd ->
                        val firstKey = matchOdd?.oddsMap?.keys?.firstOrNull()

                        matchOdd?.oddsMap?.forEach { oddsMap ->
                            oddsMap.value?.filterNotNull()?.forEach { odd ->
                                odd.isExpand = index == 0 && oddsMap.key == firstKey
                            }
                        }
                    }

                    outrightLeagueOddAdapter.data = outrightLeagueOddDataList

                    outrightOddsListResult.outrightOddsListData?.leagueOdds?.firstOrNull()?.matchOdds?.forEach { matchOdd ->
                        subscribeChannelHall(matchOdd)
                    }
                }
            }
        })

        viewModel.betInfoList.observe(this.viewLifecycleOwner, {
            it.peekContent().let {
                val odds = mutableListOf<Odd>()

                outrightLeagueOddAdapter.data.forEach { matchOdd ->
                    matchOdd?.oddsMap?.values?.forEach { oddList ->
                        odds.addAll(oddList?.filterNotNull() ?: mutableListOf())
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

                val matchOdds = outrightLeagueOddAdapter.data

                matchOdds.filterNotNull().forEachIndexed { index, matchOdd ->
                    if (SocketUpdateUtil.updateMatchOdds(context, matchOdd, oddsChangeEvent)) {
                        outrightLeagueOddAdapter.notifyItemChanged(index)
                    }
                }
            }
        })

        receiver.matchOddsLock.observe(this.viewLifecycleOwner, {
            it?.let { matchOddsLockEvent ->
                outrightLeagueOddAdapter.data.forEachIndexed { index, matchOdd ->
                    if (matchOdd?.matchInfo?.id == matchOddsLockEvent.matchId) {
                        matchOdd.oddsMap.forEach { oddsMap ->
                            oddsMap.value?.forEach { odd ->
                                odd?.status = BetStatus.LOCKED.code
                            }
                        }
                        outrightLeagueOddAdapter.notifyItemChanged(index)
                    }
                }
            }
        })

        receiver.globalStop.observe(this.viewLifecycleOwner, {
            it?.let { globalStopEvent ->

                outrightLeagueOddAdapter.data.forEachIndexed { index, matchOdd ->
                    matchOdd?.let {
                        if (SocketUpdateUtil.updateOddStatus(matchOdd, globalStopEvent)) {
                            outrightLeagueOddAdapter.notifyItemChanged(index)
                        }
                    }
                }
            }
        })

        receiver.producerUp.observe(this.viewLifecycleOwner, {
            unSubscribeChannelHallAll()

            outrightLeagueOddAdapter.data.forEach { matchOdd ->
                subscribeChannelHall(matchOdd)
            }
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

    private fun subscribeChannelHall(matchOdd: MatchOdd?) {
        val isExpand = matchOdd?.oddsMap?.values?.any {
            it?.any { odd -> odd?.isExpand ?: false } ?: false
        }

        when (isExpand) {
            true -> {
                subscribeChannelHall(
                    args.gameType.key,
                    PlayCate.OUTRIGHT.value,
                    matchOdd.matchInfo?.id
                )
            }
            false -> {
                unSubscribeChannelHall(
                    args.gameType.key,
                    PlayCate.OUTRIGHT.value,
                    matchOdd.matchInfo?.id
                )
            }
        }
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