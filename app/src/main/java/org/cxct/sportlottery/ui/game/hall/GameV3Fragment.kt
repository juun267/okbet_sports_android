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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.dialog_bottom_sheet_eps.*
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.fragment_game_v3.view.*
import kotlinx.android.synthetic.main.view_game_tab_odd_v4.view.*
import kotlinx.android.synthetic.main.view_game_toolbar_v4.*
import kotlinx.android.synthetic.main.view_game_toolbar_v4.view.*
import kotlinx.android.synthetic.main.view_match_category_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.season.Season
import org.cxct.sportlottery.network.odds.eps.EpsLeagueOddsItem
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.common.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.PlayCateUtils
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.game.hall.adapter.*
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.SpaceItemDecoration


class GameV3Fragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    private val args: GameV3FragmentArgs by navArgs()

    private val gameTypeAdapter by lazy {
        GameTypeAdapter().apply {
            gameTypeListener = GameTypeListener {
                unSubscribeChannelHallAll()

                viewModel.switchSportType(args.matchType, it)
                loading()
            }

            thirdGameListener = ThirdGameListener {
                navThirdGame(it)
            }
        }
    }

    private val dateAdapter by lazy {
        DateAdapter().apply {
            dateListener = DateListener {
                viewModel.switchMatchDate(args.matchType, it)
                loading()
            }
        }
    }

    private val matchCategoryPagerAdapter by lazy {
        MatchCategoryViewPagerAdapter(OnItemClickListener {
            navGameLeague(matchIdList = it.matchList)
        })
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
                    navGameLeague(leagueIdList = listOf(league.id))
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
            outrightCountryLeagueListener = OutrightCountryLeagueListener(
                { season ->
                    season.id?.let { navGameOutright(it) }
                },
                { leagueId ->
                    viewModel.pinFavorite(FavoriteType.LEAGUE, leagueId)
                }
            )
        }
    }

    private val leagueAdapter by lazy {
        LeagueAdapter(args.matchType).apply {
            leagueOddListener = LeagueOddListener(
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
                { matchInfo, odd, playCateName, playName ->
                    addOddsDialog(matchInfo, odd, playCateName, playName)
                },
                { matchId ->
                    matchId?.let {
                        viewModel.getQuickList(it)
                    }
                },
                {
                    viewModel.clearQuickPlayCateSelected()
                },
                { matchId ->
                    matchId?.let {
                        viewModel.pinFavorite(FavoriteType.MATCH, it)
                    }
                }
            )
        }
    }

    private val epsListAdapter by lazy {
        EpsListAdapter(EpsListAdapter.EpsOddListener({ odd, betMatchInfo ->
            addOddsDialog(
                betMatchInfo,
                odd,
                getString(R.string.game_tab_price_boosts_odd),
                odd.name ?: ""
            )
        }, { matchInfo ->
            setEpsBottomSheet(matchInfo)
        }))
    }

    private lateinit var moreEpsInfoBottomSheet: BottomSheetDialog

    val gameToolbarMatchTypeText = { matchType: MatchType ->
        when (matchType) {
            MatchType.IN_PLAY -> getString(R.string.home_tab_in_play)
            MatchType.TODAY -> getString(R.string.home_tab_today)
            MatchType.EARLY -> getString(R.string.home_tab_early)
            MatchType.PARLAY -> getString(R.string.home_tab_parlay)
            MatchType.AT_START -> getString(R.string.home_tab_at_start)
            MatchType.OUTRIGHT -> getString(R.string.home_tab_outright)
            MatchType.EPS -> getString(R.string.home_title_eps)
            else -> ""
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

            this.adapter = gameTypeAdapter

            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_sport_type
                )
            )
        }
    }

    private fun setupToolbar(view: View) {
        view.game_toolbar_match_type.text = gameToolbarMatchTypeText(args.matchType)

        view.game_toolbar_champion.apply {
            visibility = when (args.matchType) {
                MatchType.IN_PLAY -> View.VISIBLE
                else -> View.GONE
            }

            setOnClickListener {
                GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)
                    ?.let {
                        val action =
                            GameV3FragmentDirections.actionGameV3FragmentToLeagueFilterFragment(
                                it,
                                args.matchType
                            )
                        findNavController().navigate(action)
                    }
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
                    when (tab?.text.toString()) { //固定寫死
                        getString(R.string.game_tab_league_odd) -> { //賽事
                            viewModel.switchChildMatchType(childMatchType = args.matchType)
                        }
                        getString(R.string.game_tab_outright_odd) -> { //冠軍
                            viewModel.switchChildMatchType(childMatchType = MatchType.OUTRIGHT)
                        }
                        getString(R.string.game_tab_price_boosts_odd) -> { //特優賠率
                            viewModel.switchChildMatchType(childMatchType = MatchType.EPS)
                        }
                    }
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

        val epsItem = (view.game_tab_odd_v4.game_tabs.getChildAt(0) as ViewGroup).getChildAt(2)
        if (view.game_tab_odd_v4.visibility == View.VISIBLE && args.matchType == MatchType.PARLAY) {
            epsItem.visibility = View.GONE
        } else {
            epsItem.visibility = View.VISIBLE
        }
    }

    private fun setupSportBackground(view: View) {
        view.game_bg_layer2.visibility = when (args.matchType) {
            MatchType.IN_PLAY, MatchType.AT_START, MatchType.OUTRIGHT, MatchType.EPS -> View.VISIBLE
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
        view.game_match_category_pager.visibility =
            if (args.matchType == MatchType.TODAY || args.matchType == MatchType.PARLAY) {
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

            this.adapter = dateAdapter

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
            initChildMatchType()
            initObserve()
            initSocketObserver()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initChildMatchType() {
        viewModel.switchChildMatchType(null)
    }

    override fun onStart() {
        super.onStart()
        viewModel.getGameHallList(
            matchType = args.matchType,
            isReloadDate = true,
            isReloadPlayCate = true
        )
        viewModel.getMatchCategoryQuery(args.matchType)
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

                MatchType.EPS -> {
                    updateSportType(it?.sportMenuData?.menu?.eps?.items ?: listOf())
                }

            }

        })

        viewModel.matchCategoryQueryResult.observe(this.viewLifecycleOwner, {

            it.getContentIfNotHandled()?.rows?.let { resultList ->
                game_match_category_pager.visibility =
                    if ((args.matchType == MatchType.TODAY || args.matchType == MatchType.PARLAY) && resultList.isNotEmpty()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                matchCategoryPagerAdapter.data = resultList
            }
        })

        viewModel.curDate.observe(this.viewLifecycleOwner, {
            dateAdapter.data = it
        })

        viewModel.curDatePosition.observe(this.viewLifecycleOwner, {
            (game_filter_type_list.layoutManager as LinearLayoutManager?)?.scrollToPositionWithOffset(
                it, game_filter_type_list.width / 2
            )
        })

        viewModel.curChildMatchType.observe(this.viewLifecycleOwner, {
            game_toolbar_match_type.text = gameToolbarMatchTypeText(it ?: args.matchType)
        })

        viewModel.oddsListGameHallResult.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { oddsListResult ->
                hideLoading()

                if (oddsListResult.success) {
                    val leagueOdds = oddsListResult.oddsListData?.leagueOddsFilter
                        ?: oddsListResult.oddsListData?.leagueOdds ?: listOf()

                    val gameType = GameType.getGameType(oddsListResult.oddsListData?.sport?.code)

                    game_list.apply {
                        adapter = leagueAdapter.apply {
                            data = leagueOdds.onEach { leagueOdd ->
                                leagueOdd.gameType = gameType
                            }.toMutableList()
                        }
                    }

                    subscribeHallChannel()
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

        viewModel.outrightLeagueListResult.observe(this.viewLifecycleOwner, {
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

        viewModel.epsListResult.observe(this.viewLifecycleOwner, {
            hideLoading()

            val gameType =
                GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

            it.getContentIfNotHandled()?.let { epsListResult ->
                if (epsListResult.success) {
                    val oddsEpsListData = epsListResult.rows
                    val epsLeagueOddsItemList = mutableListOf<EpsLeagueOddsItem>()
                    oddsEpsListData.forEach { oddsEpdListData ->
                        val newLeagueOddsItem =
                            EpsLeagueOddsItem(
                                date = oddsEpdListData.date,
                                league = null,
                                matchOdds = null
                            )
                        epsLeagueOddsItemList.add(newLeagueOddsItem)
                        oddsEpdListData.leagueOdd.forEach { leaguesOddsItems ->
                            epsLeagueOddsItemList.add(
                                EpsLeagueOddsItem(
                                    date = 0,
                                    league = leaguesOddsItems?.league,
                                    matchOdds = leaguesOddsItems?.matchOdds
                                )
                            )
                        }
                    }
                    game_list.apply {
                        adapter = epsListAdapter.apply {
                            dataList = epsLeagueOddsItemList
                        }
                    }

                    epsLeagueOddsItemList.forEach { epsLeagueOddsItem ->
                        if (epsLeagueOddsItem.date.toInt() == 0) {
                            epsLeagueOddsItem.matchOdds?.forEach { matchOddsItem ->
                                subscribeChannelHall(
                                    gameType?.key,
                                    PlayCate.EPS.value,
                                    matchOddsItem.matchInfo?.id
                                )
                            }
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

                        matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                            quickPlayCate.quickOdds?.forEach { map ->
                                map.value.forEach { odd ->
                                    odd?.isSelected = it.any { betInfoListData ->
                                        betInfoListData.matchOdd.oddsId == odd?.id
                                    }
                                }
                            }
                        }
                    }
                }

                leagueAdapter.notifyDataSetChanged()


                val epsOdds = epsListAdapter.dataList

                epsOdds.forEach { epsLeagueOddsItem ->
                    epsLeagueOddsItem.matchOdds?.forEach { matchOddsItem ->
                        matchOddsItem.odds?.eps?.forEach { odd ->
                            odd.isSelected = it.any { betInfoListData ->
                                betInfoListData.matchOdd.oddsId == odd.id
                            }
                        }
                    }
                }
                epsListAdapter.notifyDataSetChanged()
            }
        })

        viewModel.oddsType.observe(this.viewLifecycleOwner, {
            it?.let { oddsType ->
                leagueAdapter.oddsType = oddsType
                epsListAdapter.oddsType = oddsType
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
                if (selectedPlay.selectionType == SelectionType.SELECTABLE.code) {
                    showPlayCateBottomSheet(selectedPlay)
                }
            }
        })

        viewModel.playCate.observe(this.viewLifecycleOwner, {
            playCategoryAdapter.apply {
                data.find { it.isSelected }?.playCateList?.forEach { playCate ->
                    playCate.isSelected = (playCate.code == it)
                }
                notifyDataSetChanged()
            }
        })

        viewModel.favorLeagueList.observe(this.viewLifecycleOwner, {
            updateLeaguePin(it)
            updateLeaguePinOutright(it)
        })

        viewModel.leagueSubmitList.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { leagueList ->
                navGameLeague(leagueIdList = leagueList.map { league ->
                    league.id
                })
            }
        })

        viewModel.favorMatchList.observe(this.viewLifecycleOwner, {
            leagueAdapter.data.forEach { leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    matchOdd.matchInfo?.isFavorite = it.contains(matchOdd.matchInfo?.id)
                }
            }

            leagueAdapter.notifyDataSetChanged()
        })

        viewModel.leagueFilterList.observe(this.viewLifecycleOwner, { leagueList ->
            game_toolbar_champion.isSelected = leagueList.isNotEmpty()
        })
    }

    private fun updateLeaguePin(leagueListPin: List<String>) {
        val leaguePinList = mutableListOf<League>()

        countryAdapter.data.forEach { row ->
            val pinLeague = row.list.filter { league ->
                leagueListPin.contains(league.id)
            }

            row.list.forEach { league ->
                league.isPin = leagueListPin.contains(league.id)
            }

            leaguePinList.addAll(pinLeague)
        }

        countryAdapter.datePin = leaguePinList
    }

    private fun updateLeaguePinOutright(leagueListPin: List<String>) {
        val leaguePinList = mutableListOf<Season>()

        outrightCountryAdapter.data.forEach { row ->
            val pinLeague = row.list.filter { league ->
                leagueListPin.contains(league.id)
            }

            row.list.forEach { league ->
                league.isPin = leagueListPin.contains(league.id)
            }

            leaguePinList.addAll(pinLeague)
        }

        outrightCountryAdapter.datePin = leaguePinList
    }

    private fun initSocketObserver() {
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
                                    if (matchStatusCO.status == 100) {
                                        leagueOdd.matchOdds.remove(updateMatchOdd)
                                        leagueAdapter.notifyItemRangeChanged(
                                            leagueOdds.indexOf(leagueOdd),
                                            leagueAdapter.itemCount
                                        )
                                    } else {
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
                                        GameType.FT.key -> {
                                            matchClockCO.matchTime
                                        }
                                        GameType.BK.key -> {
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
                oddsChangeEvent.updateOddsSelectedState()
                oddsChangeEvent.odds?.let { oddTypeSocketMap ->
                    val leagueOdds = leagueAdapter.data
                    val oddsType = leagueAdapter.oddsType

                    leagueOdds.forEach { leagueOdd ->
                        if (leagueOdd.isExpand) {
                            val updateMatchOdd = leagueOdd.matchOdds.find { matchOdd ->
                                matchOdd.matchInfo?.id == it.eventId
                            }

                            if (updateMatchOdd?.odds.isNullOrEmpty()) {
                                updateMatchOdd?.odds = PlayCateUtils.filterOdds(
                                    oddTypeSocketMap.toMutableMap(),
                                    updateMatchOdd?.matchInfo?.gameType ?: ""
                                )

                            } else {
                                updateMatchOdd?.odds?.forEach { oddTypeMap ->
                                    val oddsSocket = oddTypeSocketMap[oddTypeMap.key]
                                    val odds = oddTypeMap.value

                                    odds.forEach { odd ->
                                        val oddSocket = oddsSocket?.find { oddSocket ->
                                            oddSocket?.id == odd?.id
                                        }

                                        oddSocket?.let {
                                            odd?.updateOddsState(oddSocket, oddsType)
                                        }
                                    }
                                }

                                updateMatchOdd?.quickPlayCateList?.forEach { quickPlayCate ->
                                    quickPlayCate.quickOdds?.forEach { oddTypeMap ->
                                        val oddsSocket = oddTypeSocketMap[oddTypeMap.key]
                                        val odds = oddTypeMap.value

                                        odds.forEach { odd ->
                                            val oddSocket = oddsSocket?.find { oddSocket ->
                                                oddSocket?.id == odd?.id
                                            }

                                            oddSocket?.let {
                                                odd?.updateOddsState(oddSocket, oddsType)
                                            }
                                        }
                                    }
                                }
                            }

                            leagueAdapter.notifyItemChanged(
                                leagueOdds.indexOf(
                                    leagueOdd
                                )
                            )
                        }
                    }

                    val epsOdds = epsListAdapter.dataList
                    val epsOddsType = leagueAdapter.oddsType
                    val newEpsOddList = oddsChangeEvent.odds[PlayCate.EPS.value]
                    epsOdds.forEachIndexed { index, epsLeagueOddsItem ->
                        epsLeagueOddsItem.matchOdds?.forEach { matchOddsItem ->
                            matchOddsItem.odds?.eps?.forEach { epsOdd ->
                                newEpsOddList?.forEach { socketOdd ->
                                    if (socketOdd?.id == epsOdd.id) {
                                        socketOdd?.let {
                                            epsOdd.updateOddsState(socketOdd, epsOddsType)
                                        }
                                    }
                                }

                            }
                        }
                        epsListAdapter.notifyItemChanged(index)
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
                                odd?.updateOddStatus(globalStopEvent.producerId)
                            }
                        }

                        matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                            quickPlayCate.quickOdds?.values?.forEach { odds ->
                                odds.forEach { odd ->
                                    odd?.updateOddStatus(globalStopEvent.producerId)
                                }
                            }
                        }
                    }

                    leagueAdapter.notifyItemChanged(leagueOdds.indexOf(leagueOdd))
                }

                val epsOdds = epsListAdapter.dataList
                epsOdds.forEachIndexed { index, epsLeagueOddsItem ->
                    epsLeagueOddsItem.matchOdds?.forEach { matchOddsItem ->
                        matchOddsItem.odds?.eps?.forEach { epsOdd ->
                            epsOdd.updateOddStatus(globalStopEvent.producerId)
                        }
                    }
                    epsListAdapter.notifyItemChanged(index)
                }
            }
        })

        receiver.producerUp.observe(this.viewLifecycleOwner, {
            it?.let {
                unSubscribeChannelHallAll()
                subscribeHallChannel()
            }
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

    private fun Odd.updateOddsState(oddSocket: Odd, oddsType: OddsType): Odd {
        when (oddsType) {
            OddsType.EU -> {
                this.odds?.let { oddValue ->
                    oddSocket.odds?.let { oddSocketValue ->
                        when {
                            oddValue > oddSocketValue -> {
                                this.oddState =
                                    OddState.SMALLER.state
                            }
                            oddValue < oddSocketValue -> {
                                this.oddState =
                                    OddState.LARGER.state
                            }
                            oddValue == oddSocketValue -> {
                                this.oddState =
                                    OddState.SAME.state
                            }
                        }
                    }
                }
            }

            OddsType.HK -> {
                this.hkOdds?.let { oddValue ->
                    oddSocket.hkOdds?.let { oddSocketValue ->
                        when {
                            oddValue > oddSocketValue -> {
                                this.oddState =
                                    OddState.SMALLER.state
                            }
                            oddValue < oddSocketValue -> {
                                this.oddState =
                                    OddState.LARGER.state
                            }
                            oddValue == oddSocketValue -> {
                                this.oddState =
                                    OddState.SAME.state
                            }
                        }
                    }
                }
            }
        }

        this.odds = oddSocket.odds
        this.hkOdds = oddSocket.hkOdds
        this.status = oddSocket.status

        return this
    }

    private fun Odd.updateOddStatus(producerId: Int?): Odd {
        when (producerId) {
            null -> {
                this.status = BetStatus.DEACTIVATED.code
            }
            else -> {
                if (this.producerId == producerId) {
                    this.status = BetStatus.DEACTIVATED.code
                }
            }
        }
        return this
    }

    private fun setEpsBottomSheet(matchInfo: MatchInfo) {
        try {
            val contentView: ViewGroup? =
                activity?.window?.decorView?.findViewById(android.R.id.content)

            val bottomSheetView =
                layoutInflater.inflate(R.layout.dialog_bottom_sheet_eps, contentView, false)
            moreEpsInfoBottomSheet = BottomSheetDialog(this.requireContext())
            moreEpsInfoBottomSheet.apply {
                setContentView(bottomSheetView)
                btn_close.setOnClickListener {
                    this.dismiss()
                }
                tv_league_title.text = matchInfo.leagueName
                rv_more_eps_info_item.layoutManager =
                    LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
                rv_more_eps_info_item.adapter = EpsMoreInfoAdapter().apply {
                    dataList = listOf(matchInfo)
                }
            }

            moreEpsInfoBottomSheet.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateSportType(gameTypeList: List<Item>) {
        gameTypeAdapter.dataSport = gameTypeList

        gameTypeList.find { it.isSelected }?.let { item ->
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
                    GameType.FT.key -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.soccer108
                            game_bg_layer3.isVisible -> R.drawable.soccer140
                            else -> null
                        }
                    }
                    GameType.BK.key -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.basketball108
                            game_bg_layer3.isVisible -> R.drawable.basketball140
                            else -> null
                        }
                    }
                    GameType.TN.key -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.tennis108
                            game_bg_layer3.isVisible -> R.drawable.tennis140
                            else -> null
                        }
                    }
                    GameType.VB.key -> {
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
                (play.playCateList?.find { it.isSelected } ?: play.playCateList?.first())?.code,
                (play.playCateList?.find { it.isSelected } ?: play.playCateList?.first())?.name
            ),
            StatusSheetAdapter.ItemCheckedListener { _, data ->
                viewModel.switchPlayCategory(args.matchType, data.code)
                (activity as BaseActivity<*>).bottomSheet.dismiss()
                loading()
            })
    }

    private fun navThirdGame(thirdGameCategory: ThirdGameCategory) {
        val intent = Intent(activity, MainActivity::class.java)
            .putExtra(MainActivity.ARGS_THIRD_GAME_CATE, thirdGameCategory)
        startActivity(intent)
    }

    private fun navGameLeague(
        leagueIdList: List<String> = listOf(),
        matchIdList: List<String> = listOf()
    ) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        val matchType = when (dateAdapter.data.find {
            it.isSelected
        }?.date) {
            MatchType.IN_PLAY.postValue -> MatchType.IN_PLAY
            else -> null
        }

        gameType?.let {
            val action = GameV3FragmentDirections.actionGameV3FragmentToGameLeagueFragment(
                matchType ?: args.matchType,
                gameType,
                leagueIdList.toTypedArray(),
                matchIdList.toTypedArray()
            )

            findNavController().navigate(action)
        }
    }

    private fun navGameOutright(matchId: String) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        gameType?.let {
            val action =
                GameV3FragmentDirections.actionGameV3FragmentToGameOutrightFragment(
                    gameType,
                    matchId
                )

            findNavController().navigate(action)
        }
    }

    private fun navOddsDetailLive(matchId: String) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        gameType?.let {
            val action = GameV3FragmentDirections.actionGameV3FragmentToOddsDetailLiveFragment(
                gameType,
                matchId,
            )

            findNavController().navigate(action)
        }
    }

    private fun navOddsDetail(matchId: String, matchInfoList: List<MatchInfo>) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        gameType?.let {
            val action = GameV3FragmentDirections.actionGameV3FragmentToOddsDetailFragment(
                args.matchType,
                gameType,
                matchId,
                matchInfoList.toTypedArray()
            )

            findNavController().navigate(action)
        }
    }

    private fun addOddsDialog(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateName: String,
        playName: String
    ) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        gameType?.let {
            matchInfo?.let { matchInfo ->
                viewModel.updateMatchBetList(
                    args.matchType,
                    gameType,
                    playCateName,
                    playName,
                    matchInfo,
                    odd,
                    ChannelType.HALL,
                    getPlayCateMenuCode()
                )
            }
        }
    }

    private fun getPlayCateMenuCode(): String? {
        val playSelected = playCategoryAdapter.data.find { it.isSelected }

        return when (playSelected?.selectionType) {
            SelectionType.SELECTABLE.code -> {
                playSelected.playCateList?.find { it.isSelected }?.code
            }
            SelectionType.UN_SELECTABLE.code -> {
                playSelected.code
            }
            else -> null
        }
    }

    private fun subscribeHallChannel() {
        leagueAdapter.data.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->

                subscribeChannelHall(
                    leagueOdd.gameType?.key,
                    getPlayCateMenuCode(),
                    matchOdd.matchInfo?.id
                )
            }
        }

        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        epsListAdapter.dataList.forEach { epsLeagueOddsItem ->
            if (epsLeagueOddsItem.date.toInt() == 0) {
                epsLeagueOddsItem.matchOdds?.forEach { matchOddsItem ->
                    subscribeChannelHall(
                        gameType?.key,
                        PlayCate.EPS.value,
                        matchOddsItem.matchInfo?.id
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()

        unSubscribeChannelHallAll()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.clearSelectedLeague()

        game_list.adapter = null
    }
}