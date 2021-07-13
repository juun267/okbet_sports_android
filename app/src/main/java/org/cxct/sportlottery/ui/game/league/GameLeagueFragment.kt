package org.cxct.sportlottery.ui.game.league

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_game_league.*
import kotlinx.android.synthetic.main.fragment_game_league.view.*
import kotlinx.android.synthetic.main.view_game_toolbar_v4.*
import kotlinx.android.synthetic.main.view_game_toolbar_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.CateMenuCode
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.menu.OddsType


class GameLeagueFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    private val args: GameLeagueFragmentArgs by navArgs()

    private val leagueAdapter by lazy {
        LeagueAdapter(args.matchType).apply {
            leagueOddListener = LeagueOddListener(
                { matchOdd ->
                    matchOdd.matchInfo?.id?.let {
                        navOddsDetailLive(it)
                    }
                },
                { matchId, matchInfoList ->
                    when (args.matchType) {
                        MatchType.IN_PLAY -> {
                            matchId?.let {
                                navOddsDetailLive(it)
                            }
                        }
                        else -> {
                            matchId?.let {
                                navOddsDetail(it, matchInfoList)
                            }
                        }
                    }
                },
                { matchOdd, odd, playCateName, playName ->
                    addOddsDialog(matchOdd, odd, playCateName, playName)
                    hideKeyboard()
                }
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_league, container, false).apply {
            setupToolbar(this)
            setupLeagueOddList(this)
        }
    }

    private fun setupToolbar(view: View) {
        view.game_toolbar_back.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun setupLeagueOddList(view: View) {
        view.game_league_odd_list.apply {
            this.layoutManager =
                SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
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

        viewModel.getLeagueOddsList(args.matchType, args.leagueId)
        loading()
    }

    private fun initObserve() {
        viewModel.oddsListResult.observe(this.viewLifecycleOwner, {
            hideLoading()

            it.getContentIfNotHandled()?.let { oddsListResult ->
                if (oddsListResult.success) {
                    val leagueOdds = oddsListResult.oddsListData?.leagueOdds ?: listOf()

                    game_toolbar_match_type.text = oddsListResult.oddsListData?.sport?.name

                    game_league_odd_list.apply {
                        adapter = leagueAdapter.apply {
                            data = leagueOdds.onEach { leagueOdd ->
                                leagueOdd.sportType = args.sportType
                            }
                        }
                    }

                    leagueOdds.forEach { leagueOdd ->
                        leagueOdd.matchOdds.forEach { matchOdd ->
                            service.subscribeHallChannel(
                                args.sportType.code,
                                CateMenuCode.HDP_AND_OU.code,
                                matchOdd.matchInfo?.id
                            )
                        }
                    }
                }
            }
        })

        viewModel.leagueListSearchResult.observe(this.viewLifecycleOwner, {
            leagueAdapter.data = it
        })

        viewModel.betInfoList.observe(this.viewLifecycleOwner, {
            it.peekContent().let {
                val leagueOdds = leagueAdapter.data

                leagueOdds.forEach { leagueOdd ->
                    leagueOdd.matchOdds.forEach { matchOdd ->
                        matchOdd.odds.values.forEach { oddList ->
                            oddList.forEach { odd ->
                                odd?.isSelected = it.any { betInfoListData ->
                                    betInfoListData.matchOdd.oddsId == odd?.id
                                }
                            }
                        }
                    }
                }

                leagueAdapter.notifyDataSetChanged()
            }
        })

        viewModel.oddsType.observe(this.viewLifecycleOwner, {
            it?.let { oddsType ->
                leagueAdapter.oddsType = oddsType
            }
        })
    }

    private fun initSocketReceiver() {
        receiver.matchStatusChange.observe(this.viewLifecycleOwner, {
            it?.let { matchStatusChangeEvent ->
                if (args.matchType == MatchType.IN_PLAY) {

                    matchStatusChangeEvent.matchStatusCO?.let { matchStatusCO ->
                        matchStatusCO.matchId?.let { matchId ->

                            val leagueOdds = leagueAdapter.data

                            leagueOdds.forEach { leagueOdd ->
                                if (leagueOdd.isExpand) {

                                    val updateMatchOdd = leagueOdd.matchOdds.find { matchOdd ->
                                        matchOdd.matchInfo?.id == matchId
                                    }

                                    updateMatchOdd?.let {
                                        updateMatchOdd.matchInfo?.homeScore =
                                            matchStatusCO.homeScore
                                        updateMatchOdd.matchInfo?.awayScore =
                                            matchStatusCO.awayScore
                                        updateMatchOdd.matchInfo?.statusName =
                                            matchStatusCO.statusName

                                        leagueAdapter.notifyItemChanged(leagueOdds.indexOf(leagueOdd))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })

        receiver.matchClock.observe(this.viewLifecycleOwner, {
            it?.let { matchClockEvent ->
                if (args.matchType == MatchType.IN_PLAY) {

                    matchClockEvent.matchClockCO?.let { matchClockCO ->
                        matchClockCO.matchId?.let { matchId ->

                            val leagueOdds = leagueAdapter.data

                            leagueOdds.forEach { leagueOdd ->
                                if (leagueOdd.isExpand) {

                                    val updateMatchOdd = leagueOdd.matchOdds.find { matchOdd ->
                                        matchOdd.matchInfo?.id == matchId
                                    }

                                    updateMatchOdd?.let {
                                        updateMatchOdd.leagueTime = when (matchClockCO.gameType) {
                                            SportType.FOOTBALL.code -> {
                                                matchClockCO.matchTime
                                            }
                                            SportType.BASKETBALL.code -> {
                                                matchClockCO.remainingTimeInPeriod
                                            }
                                            else -> null
                                        }

                                        leagueAdapter.notifyItemChanged(leagueOdds.indexOf(leagueOdd))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })

        receiver.oddsChange.observe(this.viewLifecycleOwner, {
            it?.let { oddsChangeEvent ->
                oddsChangeEvent.odds?.let { oddTypeSocketMap ->

                    @Suppress("NAME_SHADOWING")
                    val oddTypeSocketMap = oddTypeSocketMap.mapValues { oddTypeSocketMapEntry ->
                        oddTypeSocketMapEntry.value.toMutableList().onEach { odd ->
                            odd?.isSelected =
                                viewModel.betInfoList.value?.peekContent()?.any { betInfoListData ->
                                    betInfoListData.matchOdd.oddsId == odd?.id
                                }
                        }
                    }

                    val leagueOdds = leagueAdapter.data
                    val oddsType = leagueAdapter.oddsType

                    leagueOdds.forEach { leagueOdd ->
                        if (leagueOdd.isExpand) {

                            val updateMatchOdd = leagueOdd.matchOdds.find { matchOdd ->
                                matchOdd.matchInfo?.id == oddsChangeEvent.eventId
                            }

                            if (updateMatchOdd?.odds.isNullOrEmpty()) {
                                updateMatchOdd?.odds = oddTypeSocketMap.toMutableMap()

                            } else {
                                updateMatchOdd?.odds?.forEach { oddTypeMap ->

                                    val oddsSocket = oddTypeSocketMap[oddTypeMap.key]
                                    val odds = oddTypeMap.value

                                    odds.forEach { odd ->
                                        odd?.let { oddNonNull ->
                                            val oddSocket = oddsSocket?.find { oddSocket ->
                                                oddSocket?.id == odd.id
                                            }

                                            oddSocket?.let { oddSocketNonNull ->
                                                when (oddsType) {
                                                    OddsType.EU -> {
                                                        oddNonNull.odds?.let { oddValue ->
                                                            oddSocketNonNull.odds?.let { oddSocketValue ->
                                                                when {
                                                                    oddValue > oddSocketValue -> {
                                                                        oddNonNull.oddState =
                                                                            OddState.SMALLER.state
                                                                    }
                                                                    oddValue < oddSocketValue -> {
                                                                        oddNonNull.oddState =
                                                                            OddState.LARGER.state
                                                                    }
                                                                    oddValue == oddSocketValue -> {
                                                                        oddNonNull.oddState =
                                                                            OddState.SAME.state
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                    OddsType.HK -> {
                                                        oddNonNull.hkOdds?.let { oddValue ->
                                                            oddSocketNonNull.hkOdds?.let { oddSocketValue ->
                                                                when {
                                                                    oddValue > oddSocketValue -> {
                                                                        oddNonNull.oddState =
                                                                            OddState.SMALLER.state
                                                                    }
                                                                    oddValue < oddSocketValue -> {
                                                                        oddNonNull.oddState =
                                                                            OddState.LARGER.state
                                                                    }
                                                                    oddValue == oddSocketValue -> {
                                                                        oddNonNull.oddState =
                                                                            OddState.SAME.state
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                oddNonNull.odds = oddSocketNonNull.odds
                                                oddNonNull.hkOdds = oddSocketNonNull.hkOdds

                                                oddNonNull.status = oddSocketNonNull.status

                                                leagueAdapter.notifyItemChanged(
                                                    leagueOdds.indexOf(
                                                        leagueOdd
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })

        receiver.globalStop.observe(this.viewLifecycleOwner, {
            it?.let { globalStopEvent ->

                val leagueOdds = leagueAdapter.data

                leagueOdds.forEach { leagueOdd ->
                    leagueOdd.matchOdds.forEach { matchOdd ->
                        matchOdd.odds.values.forEach { odds ->
                            odds.forEach { odd ->
                                when (globalStopEvent.producerId) {
                                    null -> {
                                        odd?.status = BetStatus.DEACTIVATED.code
                                    }
                                    else -> {
                                        odd?.producerId?.let { producerId ->
                                            if (producerId == globalStopEvent.producerId) {
                                                odd.status = BetStatus.DEACTIVATED.code
                                            }
                                        }
                                    }
                                }

                                leagueAdapter.notifyItemChanged(leagueOdds.indexOf(leagueOdd))
                            }
                        }

                    }
                }
            }
        })

        receiver.producerUp.observe(this.viewLifecycleOwner, {
            it?.let { _ ->
                service.unsubscribeAllHallChannel()

                val leagueOdds = leagueAdapter.data

                leagueOdds.forEach { leagueOdd ->
                    if (leagueOdd.isExpand) {

                        leagueOdd.matchOdds.forEach { matchOdd ->
                            service.subscribeHallChannel(
                                args.sportType.code,
                                CateMenuCode.HDP_AND_OU.code,
                                matchOdd.matchInfo?.id
                            )
                        }
                    }
                }
            }
        })
    }

    private fun navOddsDetail(matchId: String, matchInfoList: List<MatchInfo>) {
        val action =
            GameLeagueFragmentDirections.actionGameLeagueFragmentToOddsDetailFragment(
                args.matchType,
                args.sportType,
                matchId,
                matchInfoList.toTypedArray()
            )

        findNavController().navigate(action)
    }

    private fun navOddsDetailLive(matchId: String) {
        val action = GameLeagueFragmentDirections.actionGameLeagueFragmentToOddsDetailLiveFragment(
            args.sportType,
            matchId
        )

        findNavController().navigate(action)
    }

    private fun addOddsDialog(
        matchOdd: MatchOdd,
        odd: Odd,
        playCateName: String,
        playName: String
    ) {
        viewModel.updateMatchBetList(
            args.matchType,
            args.sportType,
            playCateName,
            playName,
            matchOdd,
            odd
        )
    }

    override fun onStop() {
        super.onStop()

        service.unsubscribeAllHallChannel()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        game_league_odd_list.adapter = null
    }
}