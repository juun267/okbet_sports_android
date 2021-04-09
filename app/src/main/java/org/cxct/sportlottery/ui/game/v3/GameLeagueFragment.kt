package org.cxct.sportlottery.ui.game.v3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_game_league.*
import kotlinx.android.synthetic.main.fragment_game_league.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.CateMenuCode
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameViewModel


class GameLeagueFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    private val args: GameLeagueFragmentArgs by navArgs()

    private val leagueAdapter by lazy {
        LeagueAdapter(args.matchType).apply {
            leagueOddListener = LeagueOddListener(
                { matchOdd ->
                    viewModel.getOddsDetailLive(matchOdd.matchInfo?.id)
                },
                { matchOdd ->
                    when (args.matchType) {
                        MatchType.IN_PLAY -> {
                            viewModel.getOddsDetailLive(matchOdd.matchInfo?.id)
                        }
                        else -> {
                            viewModel.getOddsDetail(matchOdd.matchInfo?.id)
                        }
                    }
                },
                { matchOdd, oddString, odd ->
                    viewModel.updateMatchBetList(matchOdd, oddString, odd)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        service.subscribeHallChannel(
            args.sportType.code,
            CateMenuCode.HDP_AND_OU.code,
            args.leagueId
        )
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

            viewModel.getLeagueOddsList(args.matchType, args.leagueId)
            loading()

        } catch (e: Exception) {
            e.printStackTrace()
        }
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

                        when {
                            (leagueOdds.isEmpty() && itemDecorationCount > 0) -> {
                                removeItemDecorationAt(0)
                            }

                            (leagueOdds.isNotEmpty() && itemDecorationCount == 0) -> {
                                addItemDecoration(
                                    DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
                                )
                            }
                        }
                    }
                }
            }
        })

        viewModel.leagueListSearchResult.observe(this.viewLifecycleOwner, Observer {
            leagueAdapter.data = it

            when {
                (it.isEmpty() && game_league_odd_list.itemDecorationCount > 0) -> {
                    game_league_odd_list.removeItemDecorationAt(0)
                }

                (it.isNotEmpty() && game_league_odd_list.itemDecorationCount == 0) -> {
                    game_league_odd_list.addItemDecoration(
                        DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
                    )
                }
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
                                            matchClockCO.remainingTime
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
                    val leagueOdds = leagueAdapter.data

                    leagueOdds.forEach { leagueOdd ->
                        if (leagueOdd.isExpand) {

                            leagueOdd.matchOdds.forEach { matchOdd ->
                                matchOdd.odds.forEach { oddTypeMap ->

                                    val oddsSocket = oddTypeSocketMap[oddTypeMap.key]
                                    val odds = oddTypeMap.value

                                    odds.forEach { odd ->
                                        odd?.let { oddNonNull ->
                                            val oddSocket = oddsSocket?.find { oddSocket ->
                                                oddSocket.id == odd.id
                                            }

                                            oddSocket?.let { oddSocketNonNull ->

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

                                                oddNonNull.odds = oddSocketNonNull.odds

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

    override fun onDestroyView() {
        super.onDestroyView()

        game_league_odd_list.adapter = null
    }

    override fun onDestroy() {
        super.onDestroy()

        service.unsubscribeAllHallChannel()
    }
}