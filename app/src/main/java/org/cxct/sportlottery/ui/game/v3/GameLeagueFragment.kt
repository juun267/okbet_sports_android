package org.cxct.sportlottery.ui.game.v3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_game_league.*
import kotlinx.android.synthetic.main.fragment_game_league.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.CateMenuCode
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameViewModel
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
                { matchOdd ->
                    when (args.matchType) {
                        MatchType.IN_PLAY -> {
                            matchOdd.matchInfo?.id?.let {
                                navOddsDetailLive(it)
                            }
                        }
                        else -> {
                            matchOdd.matchInfo?.id?.let {
                                navOddsDetail(it)
                                /*
                                 * UI上呈現只會有一項,故直接使用data[0]
                                 * 添加至投注細項(更多)
                                 */
                                viewModel.setOddsDetailMoreList(this.data[0].matchOdds)
                            }
                        }
                    }
                },
                { matchOdd, odd, playCateName, playName ->
                    addOddsDialog(matchOdd, odd, playCateName, playName)
                }
            )

            itemExpandListener = ItemExpandListener {
                when (it.isExpand) {
                    true -> {
                        it.matchOdds.forEach { matchOdd ->
                            service.subscribeHallChannel(
                                args.sportType.code,
                                CateMenuCode.HDP_AND_OU.code,
                                matchOdd.matchInfo?.id
                            )
                        }
                    }

                    false -> {
                        it.matchOdds.forEach { matchOdd ->
                            service.unsubscribeHallChannel(
                                args.sportType.code,
                                CateMenuCode.HDP_AND_OU.code,
                                matchOdd.matchInfo?.id
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_league, container, false).apply {
            setupGameFilterRow(this)
            setupLeagueOddList(this)
        }
    }

    private fun setupGameFilterRow(view: View) {
        view.game_league_filter_row.apply {

            searchHint = getString(R.string.game_filter_row_search_hint_league)

            backClickListener = View.OnClickListener {
                findNavController().navigateUp()
            }

            queryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        viewModel.searchMatch(it)
                        leagueAdapter.searchText = it
                    }
                    return true
                }
            }
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
        viewModel.oddsListResult.observe(this.viewLifecycleOwner, Observer {
            hideLoading()

            it.getContentIfNotHandled()?.let { oddsListResult ->
                if (oddsListResult.success) {
                    val leagueOdds = oddsListResult.oddsListData?.leagueOdds ?: listOf()

                    game_league_filter_row.sportName = oddsListResult.oddsListData?.sport?.name

                    game_league_odd_list.apply {
                        adapter = leagueAdapter.apply {
                            data = leagueOdds
                            sportType = args.sportType
                        }
                    }
                }
            }
        })

        viewModel.leagueListSearchResult.observe(this.viewLifecycleOwner, Observer {
            leagueAdapter.data = it
        })

        viewModel.betInfoList.observe(this.viewLifecycleOwner, Observer {
            val leagueOdds = leagueAdapter.data

            leagueOdds.forEach { leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    matchOdd.odds.values.forEach { oddList ->
                        oddList.forEach { odd ->
                            odd?.isSelected = it.any {
                                it.matchOdd.oddsId == odd?.id
                            }
                        }
                    }
                }
            }

            leagueAdapter.notifyDataSetChanged()
        })

        viewModel.oddsType.observe(this.viewLifecycleOwner, Observer {
            it?.let { oddsType ->
                leagueAdapter.oddsType = oddsType
            }
        })
    }

    private fun initSocketReceiver() {
        receiver.matchStatusChange.observe(this.viewLifecycleOwner, Observer {
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

                                    updateMatchOdd?.matchInfo?.homeScore = matchStatusCO.homeScore
                                    updateMatchOdd?.matchInfo?.awayScore = matchStatusCO.awayScore
                                    updateMatchOdd?.matchInfo?.statusName = matchStatusCO.statusName

                                    leagueAdapter.notifyItemChanged(leagueOdds.indexOf(leagueOdd))
                                }
                            }
                        }
                    }
                }
            }
        })

        receiver.matchClock.observe(this.viewLifecycleOwner, Observer {
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

                                    updateMatchOdd?.leagueTime = when (matchClockCO.gameType) {
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
        })

        receiver.oddsChange.observe(this.viewLifecycleOwner, Observer {
            it?.let { oddsChangeEvent ->
                oddsChangeEvent.odds?.let { oddTypeSocketMap ->

                    @Suppress("NAME_SHADOWING")
                    val oddTypeSocketMap = oddTypeSocketMap.mapValues { oddTypeSocketMapEntry ->
                        oddTypeSocketMapEntry.value.toMutableList().onEach { odd ->
                            odd?.isSelected =
                                viewModel.betInfoRepository.betInfoList.value?.any { betInfoListData ->
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

        receiver.globalStop.observe(this.viewLifecycleOwner, Observer {
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

        receiver.producerUp.observe(this.viewLifecycleOwner, Observer {
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

    private fun navOddsDetail(matchId: String) {
        val action =
            GameLeagueFragmentDirections.actionGameLeagueFragmentToOddsDetailFragment(
                args.matchType,
                args.sportType,
                matchId
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