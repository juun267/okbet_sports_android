package org.cxct.sportlottery.ui.game.v3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.fragment_game_v3.view.*
import kotlinx.android.synthetic.main.row_game_filter.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.CateMenuCode
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.SpaceItemDecoration


class GameV3Fragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    private val args: GameV3FragmentArgs by navArgs()

    private val sportTypeAdapter by lazy {
        SportTypeAdapter().apply {
            sportTypeListener = SportTypeListener {
                viewModel.getGameHallList(args.matchType, it)
                loading()
            }

            thirdGameListener = ThirdGameListener {
                viewModel.setGoToThirdGamePage(it)
                activity?.finish()
            }
        }
    }

    private val gameTypeAdapter by lazy {
        GameTypeAdapter().apply {
            gameTypeListener = GameTypeListener {
                viewModel.getGameHallList(args.matchType, it)
                loading()
            }
        }
    }

    private val countryAdapter by lazy {
        CountryAdapter().apply {
            countryLeagueListener = CountryLeagueListener {
                viewModel.getLeagueOddsList(args.matchType, it.id)
            }
        }
    }

    private val outrightCountryAdapter by lazy {
        OutrightCountryAdapter().apply {
            outrightCountryLeagueListener = OutrightCountryLeagueListener {
                viewModel.getOutrightOddsList(it.id)
            }
        }
    }

    private val leagueAdapter by lazy {
        LeagueAdapter(args.matchType).apply {
            leagueOddListener = LeagueOddListener(
                { matchOdd, gameCardList ->
                    viewModel.getOddsDetail(matchOdd.matchInfo?.id)
                    viewModel.gameCardList = gameCardList
                },
                { matchOdd, oddString, odd ->
                    viewModel.updateMatchBetList(matchOdd, oddString, odd)
                }
            )

            itemExpandListener = ItemExpandListener {
                val sportType = sportTypeAdapter.dataSport.find { item -> item.isSelected }?.code

                when (it.isExpand) {
                    true -> {
                        it.matchOdds.forEach { matchOdd ->
                            service.subscribeHallChannel(
                                sportType,
                                CateMenuCode.HDP_AND_OU.code,
                                matchOdd.matchInfo?.id
                            )
                        }
                    }

                    false -> {
                        it.matchOdds.forEach { matchOdd ->
                            service.unsubscribeHallChannel(
                                sportType,
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

        return inflater.inflate(R.layout.fragment_game_v3, container, false).apply {
            setupSportTypeList(this)
            setupGameFilterRow(this)
            setupGameRow(this)
            setupGameListView(this)
        }
    }

    private fun setupSportTypeList(view: View) {
        view.sport_type_list.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            this.adapter = sportTypeAdapter

            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec
                )
            )
        }
    }

    private fun setupGameFilterRow(view: View) {
        view.game_filter_row.apply {
            matchType = when (args.matchType) {
                MatchType.IN_PLAY -> GameFilterRow.IN_PLAY
                MatchType.TODAY -> GameFilterRow.TODAY
                MatchType.EARLY -> GameFilterRow.EARLY
                MatchType.PARLAY -> GameFilterRow.PARLAY
                MatchType.OUTRIGHT -> GameFilterRow.OUTRIGHT
                MatchType.AT_START -> GameFilterRow.AT_START
            }

            isSearchViewVisible = (args.matchType != MatchType.IN_PLAY)

            searchHint = getString(R.string.game_filter_row_search_hint)

            backClickListener = View.OnClickListener {
                activity?.onBackPressed()
            }

            ouHDPClickListener = View.OnClickListener {
                viewModel.setPlayType(PlayType.OU_HDP)
            }

            x12ClickListener = View.OnClickListener {
                viewModel.setPlayType(PlayType.X12)
            }

            queryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        viewModel.searchLeague(args.matchType, it)
                    }
                    return true
                }
            }
        }
    }

    private fun setupGameRow(view: View) {
        view.game_filter_type_list.apply {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            this.adapter = gameTypeAdapter

            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec
                )
            )
        }

        view.game_filter_game.visibility =
            if (args.matchType == MatchType.EARLY || args.matchType == MatchType.PARLAY || args.matchType == MatchType.OUTRIGHT) {
                View.VISIBLE
            } else {
                View.GONE
            }

        view.game_filter_game.text = when (args.matchType) {
            MatchType.EARLY, MatchType.PARLAY -> {
                resources.getString(R.string.date_row_league)
            }
            MatchType.OUTRIGHT -> {
                resources.getString(R.string.outright_row_entrance)
            }
            else -> {
                null
            }
        }

        view.game_filter_type_list.visibility =
            if (args.matchType == MatchType.EARLY || args.matchType == MatchType.PARLAY) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun setupGameListView(view: View) {
        view.game_list.apply {
            this.layoutManager =
                SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)

            this.addItemDecoration(
                DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            initObserve()
            initSocketReceiver()

            viewModel.getGameHallList(args.matchType, true)
            loading()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initObserve() {
        viewModel.sportMenuResult.observe(this.viewLifecycleOwner, Observer {
            when (args.matchType) {
                MatchType.IN_PLAY -> {
                    val itemList = it?.sportMenuData?.menu?.inPlay?.items ?: listOf()

                    sportTypeAdapter.dataSport = itemList
                    game_filter_row.sportName =
                        itemList.find { sportType -> sportType.isSelected }?.name
                }

                MatchType.TODAY -> {
                    val itemList = it?.sportMenuData?.menu?.today?.items ?: listOf()

                    sportTypeAdapter.dataSport = itemList
                    game_filter_row.sportName =
                        itemList.find { sportType -> sportType.isSelected }?.name
                }

                MatchType.EARLY -> {
                    val itemList = it?.sportMenuData?.menu?.early?.items ?: listOf()

                    sportTypeAdapter.dataSport = itemList
                    game_filter_row.sportName =
                        itemList.find { sportType -> sportType.isSelected }?.name
                }

                MatchType.PARLAY -> {
                    val itemList = it?.sportMenuData?.menu?.parlay?.items ?: listOf()

                    sportTypeAdapter.dataSport = itemList
                    game_filter_row.sportName =
                        itemList.find { sportType -> sportType.isSelected }?.name
                }

                MatchType.OUTRIGHT -> {
                    val itemList = it?.sportMenuData?.menu?.outright?.items ?: listOf()

                    sportTypeAdapter.dataSport = itemList
                    game_filter_row.sportName =
                        itemList.find { sportType -> sportType.isSelected }?.name
                }

                MatchType.AT_START -> {
                    val itemList = it?.sportMenuData?.atStart?.items ?: listOf()

                    sportTypeAdapter.dataSport = itemList
                    game_filter_row.sportName =
                        itemList.find { sportType -> sportType.isSelected }?.name
                }
            }
        })

        viewModel.gameCateDataList.observe(this.viewLifecycleOwner, Observer {
            sportTypeAdapter.dataThirdGame = it
        })

        viewModel.curPlayType.observe(viewLifecycleOwner, Observer {
            game_filter_row.playType = it
            leagueAdapter.playType = it
        })

        viewModel.curDate.observe(this.viewLifecycleOwner, Observer {
            gameTypeAdapter.data = it
        })

        viewModel.oddsListGameHallResult.observe(this.viewLifecycleOwner, Observer {
            hideLoading()

            it.getContentIfNotHandled()?.let { oddsListResult ->
                if (oddsListResult.success) {
                    game_list.adapter = leagueAdapter.apply {
                        data = oddsListResult.oddsListData?.leagueOdds ?: listOf()
                    }
                }
            }
        })

        viewModel.leagueListResult.observe(this.viewLifecycleOwner, Observer {
            hideLoading()
            clearSearchView()

            it.getContentIfNotHandled()?.let { leagueListResult ->
                if (leagueListResult.success) {
                    game_list.adapter = countryAdapter.apply {
                        data = leagueListResult.rows ?: listOf()
                    }
                }
            }
        })

        viewModel.outrightSeasonListResult.observe(this.viewLifecycleOwner, Observer {
            hideLoading()
            clearSearchView()

            it.getContentIfNotHandled()?.let { outrightSeasonListResult ->
                if (outrightSeasonListResult.success) {
                    game_list.adapter = outrightCountryAdapter.apply {
                        data = outrightSeasonListResult.rows ?: listOf()
                    }
                }
            }
        })

        viewModel.countryListSearchResult.observe(this.viewLifecycleOwner, Observer {
            countryAdapter.data = it
        })

        viewModel.outrightCountryListSearchResult.observe(this.viewLifecycleOwner, Observer {
            outrightCountryAdapter.data = it
        })

//            viewModel.isNoHistory.observe(this.viewLifecycleOwner, Observer {
//                //TODO add not history ui
//            })
    }

    private fun initSocketReceiver() {
        receiver.matchStatusChange.observe(this.viewLifecycleOwner, Observer {
            it?.let { matchStatusChangeEvent ->
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
        })

        receiver.matchClock.observe(this.viewLifecycleOwner, Observer {
            it?.let { matchClockEvent ->
                matchClockEvent.matchClockCO?.let { matchClockCO ->
                    matchClockCO.matchId?.let { matchId ->

                        val leagueOdds = leagueAdapter.data

                        leagueOdds.forEach { leagueOdd ->
                            if (leagueOdd.isExpand) {

                                val updateMatchOdd = leagueOdd.matchOdds.find { matchOdd ->
                                    matchOdd.matchInfo?.id == matchId
                                }

                                updateMatchOdd?.leagueTime = matchClockCO.matchTime

                                leagueAdapter.notifyItemChanged(leagueOdds.indexOf(leagueOdd))
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

                val sportType = sportTypeAdapter.dataSport.find { item -> item.isSelected }?.code
                val leagueOdds = leagueAdapter.data

                leagueOdds.forEach { leagueOdd ->
                    if (leagueOdd.isExpand) {

                        leagueOdd.matchOdds.forEach { matchOdd ->
                            service.subscribeHallChannel(
                                sportType,
                                CateMenuCode.HDP_AND_OU.code,
                                matchOdd.matchInfo?.id
                            )
                        }
                    }
                }
            }
        })
    }

    private fun clearSearchView() {
        game_filter_row.game_filter_search.apply {
            setQuery("", false)
            clearFocus()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        game_list.adapter = null
    }

    override fun onDestroy() {
        super.onDestroy()

        service.unsubscribeAllHallChannel()
    }
}