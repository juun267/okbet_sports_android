package org.cxct.sportlottery.ui.game.hall

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.fragment_game_v3.view.*
import kotlinx.android.synthetic.main.view_game_tab_odd_v4.view.*
import kotlinx.android.synthetic.main.view_game_toolbar_v4.*
import kotlinx.android.synthetic.main.view_game_toolbar_v4.view.*
import kotlinx.android.synthetic.main.view_match_category_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.Odd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.common.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.game.hall.adapter.*
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

                viewModel.switchSportType(args.matchType, it)
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
                viewModel.switchMatchDate(args.matchType, it)
                loading()
            }
        }
    }

    private val matchCategoryPagerAdapter by lazy {
        MatchCategoryViewPagerAdapter()
    }

    private val playCategoryAdapter by lazy {
        PlayCategoryAdapter().apply {
            playCategoryListener = PlayCategoryListener {
                viewModel.switchPlay(args.matchType, it)
                loading()
            }
        }
    }

    private val countryAdapter by lazy {
        CountryAdapter().apply {
            countryLeagueListener = CountryLeagueListener(
                { league ->
                    navGameLeague(league.id)
                },
                { league ->
                    viewModel.pinFavorite(FavoriteType.LEAGUE, league.id)
                },
                { league ->
                    viewModel.selectLeague(league)
                })
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
                { matchId, matchInfoList ->
                    when (args.matchType) {
                        MatchType.IN_PLAY -> {
                            matchId?.let {
                                navOddsDetailLive(it)
                            }
                        }
                        MatchType.AT_START -> {
                            matchId?.let {
                                navOddsDetail(it, matchInfoList)
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
            setupToolbar(this)
            setupOddTab(this)
            setupSportBackground(this)
            setupMatchCategoryPager(this)
            setupPlayCategory(this)
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
                    R.dimen.recyclerview_item_dec_spec_sport_type
                )
            )
        }
    }

    private fun setupToolbar(view: View) {
        view.game_toolbar_match_type.text = when (args.matchType) {
            MatchType.IN_PLAY -> getString(R.string.home_tab_in_play)
            MatchType.TODAY -> getString(R.string.home_tab_today)
            MatchType.EARLY -> getString(R.string.home_tab_early)
            MatchType.PARLAY -> getString(R.string.home_tab_parlay)
            MatchType.AT_START -> getString(R.string.home_tab_at_start)
            MatchType.OUTRIGHT -> getString(R.string.home_tab_outright)
            else -> ""
        }

        view.game_toolbar_champion.apply {
            visibility = when (args.matchType) {
                MatchType.IN_PLAY -> View.VISIBLE
                else -> View.GONE
            }

            setOnClickListener {
                Toast.makeText(context, "click toolbar champion", Toast.LENGTH_SHORT).show()
            }
        }

        view.game_toolbar_calendar.apply {
            visibility = when (args.matchType) {
                MatchType.EARLY -> View.VISIBLE
                else -> View.GONE
            }

            setOnClickListener {
                isSelected = !isSelected

                view.game_filter_type_list.visibility = when (isSelected) {
                    true -> View.VISIBLE
                    false -> View.GONE
                }
            }
        }

        view.game_toolbar_back.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun setupOddTab(view: View) {
        view.game_tabs.apply {
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    //TODO add odd tab switch behavior
                    Toast.makeText(requireContext(), "${tab?.text} is selected", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }
            })
        }

        view.game_tab_odd_v4.visibility = when (args.matchType) {
            MatchType.TODAY, MatchType.EARLY, MatchType.PARLAY -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun setupSportBackground(view: View) {
        view.game_bg_layer2.visibility = when (args.matchType) {
            MatchType.IN_PLAY, MatchType.AT_START, MatchType.OUTRIGHT -> View.VISIBLE
            else -> View.GONE
        }

        view.game_bg_layer3.visibility = when (args.matchType) {
            MatchType.TODAY, MatchType.EARLY, MatchType.PARLAY -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun setupMatchCategoryPager(view: View) {
        view.match_category_pager.adapter = matchCategoryPagerAdapter
        view.match_category_indicator.setupWithViewPager2(view.match_category_pager)
        view.game_match_category_pager.visibility = if (args.matchType == MatchType.TODAY) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun setupPlayCategory(view: View) {
        view.game_play_category.apply {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            this.adapter = playCategoryAdapter

            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_play_category
                )
            )

            visibility =
                if (args.matchType == MatchType.IN_PLAY || args.matchType == MatchType.AT_START) {
                    View.VISIBLE
                } else {
                    View.GONE
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
                    R.dimen.recyclerview_item_dec_spec_date
                )
            )
        }

        view.game_filter_type_list.visibility =
            if (args.matchType == MatchType.EARLY && view.game_toolbar_calendar.isSelected) {
                View.VISIBLE
            } else {
                View.GONE
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

        viewModel.getGameHallList(args.matchType, true, isReloadPlayCate = true)
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
                else -> {
                }
            }
        })

        viewModel.curDate.observe(this.viewLifecycleOwner, {
            gameTypeAdapter.data = it
        })

        viewModel.curDatePosition.observe(this.viewLifecycleOwner, {
            (game_filter_type_list.layoutManager as LinearLayoutManager?)?.scrollToPositionWithOffset(
                it, game_filter_type_list.width / 2
            )
        })

        viewModel.oddsListGameHallResult.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { oddsListResult ->
                hideLoading()

                if (oddsListResult.success) {
                    val leagueOdds = oddsListResult.oddsListData?.leagueOdds ?: listOf()

                    val sportType = SportType.getSportType(oddsListResult.oddsListData?.sport?.code)

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

        viewModel.leagueListResult.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { leagueListResult ->
                hideLoading()

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

        viewModel.outrightSeasonListResult.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { outrightSeasonListResult ->
                hideLoading()

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

        viewModel.countryListSearchResult.observe(this.viewLifecycleOwner, {
            countryAdapter.data = it
        })

        viewModel.outrightCountryListSearchResult.observe(this.viewLifecycleOwner, {
            outrightCountryAdapter.data = it
        })

        viewModel.isNoHistory.observe(this.viewLifecycleOwner, {
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

        viewModel.leagueSelectedList.observe(this.viewLifecycleOwner, {
            countryAdapter.apply {
                data.forEach { row ->
                    row.list.forEach { league ->
                        league.isSelected = it.any { it.id == league.id }
                    }
                }

                notifyDataSetChanged()
            }
        })

        viewModel.playList.observe(this.viewLifecycleOwner, {
            playCategoryAdapter.data = it

            it.find { play ->
                play.isSelected
            }?.let { selectedPlay ->
                if (selectedPlay.code != PlayType.MAIN.code && selectedPlay.playCateList?.size ?: 0 > 1) {
                    selectedPlay.let {
                        showPlayCateBottomSheet(selectedPlay)
                        hideLoading()
                    }
                }
            }
        })

        viewModel.favorLeagueList.observe(this.viewLifecycleOwner, {
            val leaguePinList = mutableListOf<League>()

            countryAdapter.data.forEach { row ->
                val pinLeague = row.list.filter { league ->
                    it.contains(league.id)
                }

                row.list.forEach { league ->
                    league.isPin = it.contains(league.id)
                }

                leaguePinList.addAll(pinLeague)
            }

            countryAdapter.datePin = leaguePinList
        })
    }

    private fun initSocketReceiver() {
        receiver.matchStatusChange.observe(this.viewLifecycleOwner, {
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

        receiver.matchClock.observe(this.viewLifecycleOwner, {
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
        sportTypeAdapter.dataSport = sportTypeList

        sportTypeList.find { it.isSelected }?.let { item ->
            game_toolbar_sport_type.text = item.name
            updateSportBackground(item)
        }
    }

    private fun updateSportBackground(sport: Item) {
        when {
            game_bg_layer2.isVisible -> game_bg_layer2
            game_bg_layer3.isVisible -> game_bg_layer3
            else -> null
        }?.let {
            Glide.with(requireContext()).load(
                when (sport.code) {
                    SportType.FOOTBALL.code -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.soccer108
                            game_bg_layer3.isVisible -> R.drawable.soccer140
                            else -> null
                        }
                    }
                    SportType.BASKETBALL.code -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.basketball108
                            game_bg_layer3.isVisible -> R.drawable.basketball140
                            else -> null
                        }
                    }
                    SportType.TENNIS.code -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.tennis108
                            game_bg_layer3.isVisible -> R.drawable.tennis140
                            else -> null
                        }
                    }
                    SportType.VOLLEYBALL.code -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.volleyball108
                            game_bg_layer3.isVisible -> R.drawable.volleyball140
                            else -> null
                        }
                    }
                    else -> null
                }
            ).into(it)
        }
    }

    private fun showPlayCateBottomSheet(play: Play) {
        showBottomSheetDialog(
            play.name,
            play.playCateList?.map { playCate -> StatusSheetData(playCate.code, playCate.name) }
                ?: listOf(),
            StatusSheetData(
                play.playCateList?.first()?.code,
                play.playCateList?.first()?.name
            ),
            StatusSheetAdapter.ItemCheckedListener { _, data ->

            })
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

    private fun navOddsDetail(matchId: String, matchInfoList: List<MatchInfo>) {
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
                matchId,
                matchInfoList.toTypedArray()
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