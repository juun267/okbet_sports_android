package org.cxct.sportlottery.ui.game.hall

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.fragment_game_v3.view.*
import kotlinx.android.synthetic.main.row_game_filter.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.CateMenuCode
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.game.hall.adapter.CountryAdapter
import org.cxct.sportlottery.ui.game.hall.adapter.OutrightCountryAdapter
import org.cxct.sportlottery.ui.game.widget.GameFilterRow
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.SpaceItemDecoration


class GameV3Fragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    private val args: GameV3FragmentArgs by navArgs()

    private val sportTypeAdapter by lazy {
        SportTypeAdapter().apply {
            sportTypeListener = SportTypeListener {
                service.unsubscribeAllHallChannel()

                viewModel.getGameHallList(args.matchType, it)
                loading()
            }

            thirdGameListener = ThirdGameListener {
                navThirdGame(it)
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
            countryLeagueListener = CountryLeagueListener { league ->
                navGameLeague(league.id)
            }
        }
    }

    private val outrightCountryAdapter by lazy {
        OutrightCountryAdapter().apply {
            outrightCountryLeagueListener = OutrightCountryLeagueListener { season ->
                navGameOutright(season.id)
            }
        }
    }

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
                        MatchType.AT_START -> {
                            matchOdd.matchInfo?.id?.let {
                                navOddsDetail(it)
                                viewModel.setOddsDetailMoreList(
                                    data.find { dataList -> dataList.matchOdds.find { dataMatchOdds -> dataMatchOdds == matchOdd } == matchOdd }?.matchOdds?.toList()
                                        ?: listOf<MatchOdd>()
                                )
                            }
                        }
                        else -> {
                        }
                    }
                },
                { matchOdd, odd, playCateName, playName ->
                    addOddsDialog(matchOdd, odd, playCateName, playName)
                }
            )
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

            isSearchViewVisible =
                (args.matchType != MatchType.IN_PLAY && args.matchType != MatchType.AT_START)

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
                        countryAdapter.searchText = it
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

        view.game_filter_divider.visibility = if (args.matchType == MatchType.OUTRIGHT) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    private fun setupGameListView(view: View) {
        view.game_list.apply {
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

        viewModel.getGameHallList(args.matchType, true)
        loading()
    }

    private fun initObserve() {
        viewModel.sportMenuResult.observe(this.viewLifecycleOwner, {
            when (args.matchType) {
                MatchType.IN_PLAY -> {
                    updateSportType(it?.sportMenuData?.menu?.inPlay?.items ?: listOf())
                }

                MatchType.TODAY -> {
                    updateSportType(it?.sportMenuData?.menu?.today?.items ?: listOf())
                }

                MatchType.EARLY -> {
                    updateSportType(it?.sportMenuData?.menu?.early?.items ?: listOf())
                }

                MatchType.PARLAY -> {
                    updateSportType(it?.sportMenuData?.menu?.parlay?.items ?: listOf())
                }

                MatchType.OUTRIGHT -> {
                    updateSportType(it?.sportMenuData?.menu?.outright?.items ?: listOf())
                }

                MatchType.AT_START -> {
                    updateSportType(it?.sportMenuData?.atStart?.items ?: listOf())
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

        viewModel.curDatePosition.observe(this.viewLifecycleOwner, Observer {
            (game_filter_type_list.layoutManager as LinearLayoutManager?)?.scrollToPositionWithOffset(
                it, game_filter_type_list.width / 2
            )
        })

        viewModel.oddsListGameHallResult.observe(this.viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { oddsListResult ->
                hideLoading()

                if (oddsListResult.success) {
                    val leagueOdds = oddsListResult.oddsListData?.leagueOdds ?: listOf()

                    val sportType = when (oddsListResult.oddsListData?.sport?.code) {
                        SportType.FOOTBALL.code -> SportType.FOOTBALL
                        SportType.BASKETBALL.code -> SportType.BASKETBALL
                        SportType.BADMINTON.code -> SportType.BADMINTON
                        SportType.VOLLEYBALL.code -> SportType.VOLLEYBALL
                        SportType.TENNIS.code -> SportType.TENNIS
                        else -> null
                    }

                    game_list.apply {
                        adapter = leagueAdapter.apply {
                            data = leagueOdds.onEach { leagueOdd ->
                                leagueOdd.sportType = sportType
                            }
                        }
                    }

                    leagueOdds.forEach { leagueOdd ->
                        leagueOdd.matchOdds.forEach { matchOdd ->
                            service.subscribeHallChannel(
                                sportType?.code,
                                CateMenuCode.HDP_AND_OU.code,
                                matchOdd.matchInfo?.id
                            )
                        }
                    }
                }
            }
        })

        viewModel.leagueListResult.observe(this.viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { leagueListResult ->
                hideLoading()
                clearSearchView()

                if (leagueListResult.success) {
                    val rows = leagueListResult.rows ?: listOf()

                    game_list.apply {
                        adapter = countryAdapter.apply {
                            data = rows
                        }
                    }
                }
            }
        })

        viewModel.outrightSeasonListResult.observe(this.viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { outrightSeasonListResult ->
                hideLoading()
                clearSearchView()

                if (outrightSeasonListResult.success) {
                    val rows = outrightSeasonListResult.rows ?: listOf()

                    game_list.apply {
                        adapter = outrightCountryAdapter.apply {
                            data = rows
                        }
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

        viewModel.isNoHistory.observe(this.viewLifecycleOwner, Observer {
            if (it) {
                hideLoading()
            }

            game_no_record.apply {
                setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))

                visibility = if (it) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }

            game_no_record_bg.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
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
                matchStatusChangeEvent.matchStatusCO?.let { matchStatusCO ->
                    matchStatusCO.matchId?.let { matchId ->

                        val leagueOdds = leagueAdapter.data

                        leagueOdds.forEach { leagueOdd ->
                            if (leagueOdd.isExpand) {

                                val updateMatchOdd = leagueOdd.matchOdds.find { matchOdd ->
                                    matchOdd.matchInfo?.id == matchId
                                }

                                updateMatchOdd?.let {
                                    updateMatchOdd.matchInfo?.homeScore = matchStatusCO.homeScore
                                    updateMatchOdd.matchInfo?.awayScore = matchStatusCO.awayScore
                                    updateMatchOdd.matchInfo?.statusName = matchStatusCO.statusName

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

    private fun updateSportType(sportTypeList: List<Item>) {
        val selectedSportType = sportTypeList.find { sportType -> sportType.isSelected }

        sportTypeAdapter.dataSport = sportTypeList

        game_filter_row.apply {
            sportName = selectedSportType?.name

            isPlayTypeVisible =
                (selectedSportType?.code == SportType.FOOTBALL.code) || (selectedSportType?.code == SportType.BASKETBALL.code)
        }

        viewModel.setPlayType(PlayType.OU_HDP)
    }

    private fun clearSearchView() {
        game_filter_row.game_filter_search.apply {
            setQuery("", false)
            clearFocus()
        }
    }

    private fun navThirdGame(thirdGameCategory: ThirdGameCategory) {
        val intent = Intent(activity, MainActivity::class.java)
            .putExtra(MainActivity.ARGS_THIRD_GAME_CATE, thirdGameCategory)
        startActivity(intent)
    }

    private fun navGameLeague(matchId: String) {
        val sportType =
            when (sportTypeAdapter.dataSport.find { item -> item.isSelected }?.code) {
                SportType.FOOTBALL.code -> SportType.FOOTBALL
                SportType.BASKETBALL.code -> SportType.BASKETBALL
                SportType.VOLLEYBALL.code -> SportType.VOLLEYBALL
                SportType.BADMINTON.code -> SportType.BADMINTON
                SportType.TENNIS.code -> SportType.TENNIS
                else -> null
            }

        val matchType = when (gameTypeAdapter.data.find {
            it.isSelected
        }?.date) {
            MatchType.IN_PLAY.postValue -> MatchType.IN_PLAY
            else -> null
        }

        sportType?.let {
            val action = GameV3FragmentDirections.actionGameV3FragmentToGameLeagueFragment(
                matchType ?: args.matchType,
                sportType,
                matchId
            )

            findNavController().navigate(action)
        }
    }

    private fun navGameOutright(matchId: String) {
        val sportType =
            when (sportTypeAdapter.dataSport.find { item -> item.isSelected }?.code) {
                SportType.FOOTBALL.code -> SportType.FOOTBALL
                SportType.BASKETBALL.code -> SportType.BASKETBALL
                SportType.VOLLEYBALL.code -> SportType.VOLLEYBALL
                SportType.BADMINTON.code -> SportType.BADMINTON
                SportType.TENNIS.code -> SportType.TENNIS
                else -> null
            }

        sportType?.let {
            val action =
                GameV3FragmentDirections.actionGameV3FragmentToGameOutrightFragment(
                    sportType,
                    matchId
                )

            findNavController().navigate(action)
        }
    }

    private fun navOddsDetailLive(matchId: String) {
        val sportType =
            when (sportTypeAdapter.dataSport.find { item -> item.isSelected }?.code) {
                SportType.FOOTBALL.code -> SportType.FOOTBALL
                SportType.BASKETBALL.code -> SportType.BASKETBALL
                SportType.VOLLEYBALL.code -> SportType.VOLLEYBALL
                SportType.BADMINTON.code -> SportType.BADMINTON
                SportType.TENNIS.code -> SportType.TENNIS
                else -> null
            }

        sportType?.let {
            val action = GameV3FragmentDirections.actionGameV3FragmentToOddsDetailLiveFragment(
                sportType,
                matchId,
            )

            findNavController().navigate(action)
        }
    }

    private fun navOddsDetail(matchId: String) {
        val sportType =
            when (sportTypeAdapter.dataSport.find { item -> item.isSelected }?.code) {
                SportType.FOOTBALL.code -> SportType.FOOTBALL
                SportType.BASKETBALL.code -> SportType.BASKETBALL
                SportType.VOLLEYBALL.code -> SportType.VOLLEYBALL
                SportType.BADMINTON.code -> SportType.BADMINTON
                SportType.TENNIS.code -> SportType.TENNIS
                else -> null
            }

        sportType?.let {
            val action = GameV3FragmentDirections.actionGameV3FragmentToOddsDetailFragment(
                args.matchType,
                sportType,
                matchId
            )

            findNavController().navigate(action)
        }
    }

    private fun addOddsDialog(
        matchOdd: MatchOdd,
        odd: Odd,
        playCateName: String,
        playName: String
    ) {
        val sportType =
            when (sportTypeAdapter.dataSport.find { item -> item.isSelected }?.code) {
                SportType.FOOTBALL.code -> SportType.FOOTBALL
                SportType.BASKETBALL.code -> SportType.BASKETBALL
                SportType.VOLLEYBALL.code -> SportType.VOLLEYBALL
                SportType.BADMINTON.code -> SportType.BADMINTON
                SportType.TENNIS.code -> SportType.TENNIS
                else -> null
            }

        sportType?.let {
            viewModel.updateMatchBetList(
                args.matchType,
                sportType,
                playCateName,
                playName,
                matchOdd,
                odd
            )
        }
    }

    override fun onStop() {
        super.onStop()

        service.unsubscribeAllHallChannel()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        game_list.adapter = null
    }
}