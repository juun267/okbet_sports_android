package org.cxct.sportlottery.ui.game.hall

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.view_game_tab_odd_v4.*
import kotlinx.android.synthetic.main.view_game_tab_odd_v4.view.*
import kotlinx.android.synthetic.main.view_game_toolbar_v4.*
import kotlinx.android.synthetic.main.view_game_toolbar_v4.view.*
import kotlinx.android.synthetic.main.view_match_category_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsLeagueOddsItem
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.outright.season.Season
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
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.common.LeagueListener
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.game.hall.adapter.*
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.statistics.KEY_MATCH_ID
import org.cxct.sportlottery.ui.statistics.StatisticsActivity
import org.cxct.sportlottery.util.SocketUpdateUtil
import org.cxct.sportlottery.util.SpaceItemDecoration
import timber.log.Timber


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
                if (it.selectionType == SelectionType.SELECTABLE.code) { //被鎖 或是不能下拉
                    when {
                        //這個是沒有點選過的狀況 第一次進來 ：開啟選單
                        !it.isSelected && it.isLocked == null -> {
                            showPlayCateBottomSheet(it)
                        }
                        //當前被點選的狀態
                        it.isSelected -> {
                            showPlayCateBottomSheet(it)
                        }
                        //之前點選過然後離開又回來 要預設帶入
                        !it.isSelected && it.isLocked == false -> {
                            viewModel.switchPlay(args.matchType, it)
                            loading()
                        }
                    }
                } else {
                    viewModel.switchPlay(args.matchType, it)
                    upDateSelectPlay(it)
                    loading()
                }
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
            leagueListener = LeagueListener {
                subscribeChannelHall(it)
            }
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
                { matchInfo, odd, playCateName ->
                    addOddsDialog(matchInfo, odd, playCateName)
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
                },
                { matchId ->
                    navStatistics(matchId)
                }
            )
        }
    }

    private val epsListAdapter by lazy {
        EpsListAdapter(EpsListAdapter.EpsOddListener(
            {
                subscribeChannelHall(it)
            },
            { odd, betMatchInfo ->
                addOddsDialog(
                    betMatchInfo,
                    odd,
                    getString(R.string.game_tab_price_boosts_odd)
                )
            }, { matchInfo ->
                setEpsBottomSheet(matchInfo)
            })
        )
    }

    private lateinit var moreEpsInfoBottomSheet: BottomSheetDialog

    val gameToolbarMatchTypeText = { matchType: MatchType ->
        when (matchType) {
            MatchType.IN_PLAY -> getString(R.string.home_tab_in_play)
            MatchType.TODAY -> getString(R.string.home_tab_today)
            MatchType.EARLY -> getString(R.string.home_tab_early)
            MatchType.PARLAY -> getString(R.string.home_tab_parlay)
            MatchType.AT_START -> getString(R.string.home_tab_at_start_2)
            MatchType.OUTRIGHT -> getString(R.string.home_tab_outright)
            MatchType.EPS -> getString(R.string.home_title_eps)
            else -> ""
        }
    }

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
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
                MatchType.IN_PLAY, MatchType.AT_START -> View.VISIBLE
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
            addOnTabSelectedListener(onTabSelectedListener)
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

            addItemDecoration(
                SpaceItemDecoration(context, R.dimen.item_spacing_league)
            )
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
        viewModel.getGameHallList(
            matchType = args.matchType,
            isReloadDate = true,
            isReloadPlayCate = true,
            isLastSportType = true
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

                else -> {
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
            //TODO childMatchType更新選中
            //預設第一項
            when (it) {
                null -> {
                    //init tab select
                    game_tabs.clearOnTabSelectedListeners()
                    game_tabs.selectTab(game_tabs.getTabAt(0))
                    game_tabs.addOnTabSelectedListener(onTabSelectedListener)
                }
                MatchType.OUTRIGHT -> {
                    game_tabs.selectTab(game_tabs.getTabAt(1))
                }
                MatchType.EPS -> {
                    game_tabs.selectTab(game_tabs.getTabAt(2))
                }
                else -> {
                    game_tabs.selectTab(game_tabs.getTabAt(0))
                }
            }
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

                    leagueOdds.forEach { leagueOdd ->
                        subscribeChannelHall(leagueOdd)
                    }
                }
            }
        })

        viewModel.oddsListGameHallIncrementResult.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { leagueListIncrementResult ->
                val leagueListIncrement = leagueListIncrementResult.oddsListResult

                leagueListIncrementResult.leagueIdList?.forEach { leagueId ->
                    //判斷此聯賽是否已經於畫面顯示
                    leagueAdapter.data.find { adapterLeagueOdd -> adapterLeagueOdd.league.id == leagueId }
                        ?.let { onScreenLeague ->

                            val targetIndex = leagueAdapter.data.indexOf(onScreenLeague)
                            val changedLeague =
                                leagueListIncrement?.oddsListData?.leagueOdds?.find { leagueOdd -> leagueOdd.league.id == onScreenLeague.league.id }

                            //若api response沒有該聯賽資料則不顯示該聯賽
                            changedLeague?.let { changedLeagueOdd ->
                                unSubscribeLeagueChannelHall(leagueAdapter.data[targetIndex])
                                val targetLeagueOdd = leagueAdapter.data[targetIndex]
                                leagueAdapter.data[targetIndex] = changedLeagueOdd.apply {
                                    this.unfold = targetLeagueOdd.unfold
                                    this.gameType = targetLeagueOdd.gameType
                                    this.searchMatchOdds = targetLeagueOdd.searchMatchOdds
                                }
                                subscribeChannelHall(leagueAdapter.data[targetIndex])
                            } ?: run {
                                leagueAdapter.data.removeAt(targetIndex)
                                leagueAdapter.notifyItemRemoved(targetIndex)
                            }
                        } ?: run {
                        //不在畫面上的League
                        val changedLeague =
                            leagueListIncrement?.oddsListData?.leagueOdds?.find { leagueOdd -> leagueOdd.league.id == leagueId }
                        changedLeague?.let { changedLeagueOdd ->
                            val gameType = GameType.getGameType(leagueListIncrement.oddsListData.sport.code)
                            val insertLeagueOdd = changedLeagueOdd.apply {
                                this.gameType = gameType
                            }
                            leagueAdapter.data.add(insertLeagueOdd)
                            leagueAdapter.notifyItemInserted(leagueAdapter.data.size - 1)
                            subscribeChannelHall(insertLeagueOdd)
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

            it.getContentIfNotHandled()?.let { epsListResult ->
                if (epsListResult.success) {
                    val oddsEpsListDatas = epsListResult.rows
                    val epsLeagueOddsItemList = mutableListOf<EpsLeagueOddsItem>()

                    oddsEpsListDatas.forEachIndexed { indexDate, oddsEpsListData ->
                        val newLeagueOddsItem =
                            EpsLeagueOddsItem(
                                date = oddsEpsListData.date,
                                league = null,
                                matchOdds = null
                            )
                        epsLeagueOddsItemList.add(newLeagueOddsItem)

                        oddsEpsListData.leagueOdd.forEachIndexed { indexLeague, leagueOdds ->
                            epsLeagueOddsItemList.add(
                                EpsLeagueOddsItem(
                                    date = 0,
                                    league = leagueOdds?.league,
                                    matchOdds = leagueOdds?.matchOdds
                                ).apply {
                                    isClose = !(indexDate == 0 && indexLeague == 0)
                                }
                            )
                        }
                    }

                    game_list.apply {
                        adapter = epsListAdapter.apply {
                            dataList = epsLeagueOddsItemList
                        }
                    }

                    epsLeagueOddsItemList.forEach { epsLeagueOddsItem ->
                        subscribeChannelHall(epsLeagueOddsItem)
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
                        matchOdd.oddsMap.values.forEach { oddList ->
                            oddList?.forEach { odd ->
                                odd?.isSelected = it.any { betInfoListData ->
                                    betInfoListData.matchOdd.oddsId == odd?.id
                                }
                            }
                        }

                        matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                            quickPlayCate.quickOdds?.forEach { map ->
                                map.value?.forEach { odd ->
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
                        matchOddsItem.oddsEps?.eps?.forEach { odd ->
                            odd?.isSelected = it.any { betInfoListData ->
                                betInfoListData.matchOdd.oddsId == odd?.id
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

        countryAdapter.datePin = leaguePinList.sortedBy {
            leagueListPin.indexOf(it.id)
        }
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

        outrightCountryAdapter.datePin = leaguePinList.sortedBy {
            leagueListPin.indexOf(it.id)
        }
    }

    private fun initSocketObserver() {
        receiver.matchStatusChange.observe(this.viewLifecycleOwner, {
            it?.let { matchStatusChangeEvent ->
                when (game_list.adapter) {
                    is LeagueAdapter -> {
                        val leagueOdds = leagueAdapter.data

                        leagueOdds.forEachIndexed { index, leagueOdd ->
                            if (SocketUpdateUtil.updateMatchStatus(
                                    gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code,
                                    leagueOdd.matchOdds.toMutableList(),
                                    matchStatusChangeEvent
                                ) &&
                                leagueOdd.unfold == FoldState.UNFOLD.code
                            ) {
                                if (leagueOdd.matchOdds.isNullOrEmpty()) {
                                    leagueAdapter.data.remove(leagueOdd)
                                }

                                leagueAdapter.notifyItemChanged(index)
                            }
                        }
                    }
                }
            }
        })

        receiver.matchClock.observe(this.viewLifecycleOwner, {
            it?.let { matchClockEvent ->
                when (game_list.adapter) {
                    is LeagueAdapter -> {
                        val leagueOdds = leagueAdapter.data

                        leagueOdds.forEachIndexed { index, leagueOdd ->
                            if (leagueOdd.matchOdds.any { matchOdd ->
                                    SocketUpdateUtil.updateMatchClock(
                                        matchOdd,
                                        matchClockEvent
                                    )
                                } &&
                                leagueOdd.unfold == FoldState.UNFOLD.code) {

                                leagueAdapter.notifyItemChanged(index)
                            }
                        }
                    }
                }
            }
        })

        receiver.oddsChange.observe(this.viewLifecycleOwner, {
            it?.let { oddsChangeEvent ->
                oddsChangeEvent.updateOddsSelectedState()

                when (game_list.adapter) {
                    is LeagueAdapter -> {
                        val leagueOdds = leagueAdapter.data

                        leagueOdds.forEachIndexed { index, leagueOdd ->
                            if (leagueOdd.matchOdds.any { matchOdd ->
                                    SocketUpdateUtil.updateMatchOdds(
                                        context, matchOdd, oddsChangeEvent
                                    )
                                } &&
                                leagueOdd.unfold == FoldState.UNFOLD.code
                            ) {
                                leagueAdapter.notifyItemChanged(index)
                            }
                        }
                    }

                    is EpsListAdapter -> {
                        val epsOdds = epsListAdapter.dataList

                        epsOdds.forEachIndexed { index, leagueOdd ->
                            if (leagueOdd.matchOdds?.any { matchOdd ->
                                    SocketUpdateUtil.updateMatchOdds(
                                        context, matchOdd, oddsChangeEvent
                                    )
                                } == true &&
                                !leagueOdd.isClose) {

                                epsListAdapter.notifyItemChanged(index)
                            }
                        }
                    }
                }
            }
        })

        receiver.matchOddsLock.observe(this.viewLifecycleOwner, {
            it?.let { matchOddsLockEvent ->
                when (game_list.adapter) {
                    is LeagueAdapter -> {
                        val leagueOdds = leagueAdapter.data

                        leagueOdds.forEachIndexed { index, leagueOdd ->
                            if (leagueOdd.matchOdds.any { matchOdd ->
                                    SocketUpdateUtil.updateOddStatus(matchOdd, matchOddsLockEvent)
                                } && leagueOdd.unfold == FoldState.UNFOLD.code) {
                                leagueAdapter.notifyItemChanged(index)
                            }
                        }
                    }
                    is EpsListAdapter -> {
                        val epsOdds = epsListAdapter.dataList

                        epsOdds.forEachIndexed { index, leagueOdd ->
                            if (leagueOdd.matchOdds?.any { matchOdd ->
                                    SocketUpdateUtil.updateOddStatus(matchOdd, matchOddsLockEvent)
                                } == true && !leagueOdd.isClose) {
                                epsListAdapter.notifyItemChanged(index)
                            }
                        }
                    }
                }
            }
        })

        receiver.globalStop.observe(this.viewLifecycleOwner, {
            it?.let { globalStopEvent ->

                when (game_list.adapter) {
                    is LeagueAdapter -> {
                        val leagueOdds = leagueAdapter.data

                        leagueOdds.forEachIndexed { index, leagueOdd ->
                            if (leagueOdd.matchOdds.any { matchOdd ->
                                    SocketUpdateUtil.updateOddStatus(
                                        matchOdd,
                                        globalStopEvent
                                    )
                                } &&
                                leagueOdd.unfold == FoldState.UNFOLD.code
                            ) {
                                leagueAdapter.notifyItemChanged(index)
                            }
                        }
                    }

                    is EpsListAdapter -> {
                        val epsOdds = epsListAdapter.dataList

                        epsOdds.forEachIndexed { index, epsLeagueOddsItem ->
                            if (epsLeagueOddsItem.matchOdds?.any { matchOdd ->
                                    SocketUpdateUtil.updateOddStatus(
                                        matchOdd,
                                        globalStopEvent
                                    )
                                } == true &&
                                !epsLeagueOddsItem.isClose) {

                                epsListAdapter.notifyItemChanged(index)
                            }
                        }
                    }
                }
            }
        })

        receiver.producerUp.observe(this.viewLifecycleOwner, {
            it?.let {
                unSubscribeChannelHallAll()

                when (game_list.adapter) {
                    is LeagueAdapter -> {
                        leagueAdapter.data.forEach { leagueOdd ->
                            subscribeChannelHall(leagueOdd)
                        }
                    }

                    is EpsListAdapter -> {
                        epsListAdapter.dataList.forEach { epsLeagueOddsItem ->
                            subscribeChannelHall(epsLeagueOddsItem)
                        }
                    }
                }
            }
        })

        receiver.leagueChange.observe(this.viewLifecycleOwner, {
            it?.let { leagueChangeEvent ->
                when (game_list.adapter) {
                    is LeagueAdapter, is CountryAdapter, is OutrightCountryAdapter -> {
                        leagueChangeEvent.leagueIdList?.let { leagueIdList ->
                            viewModel.getGameHallList(args.matchType,
                                isReloadDate = false,
                                leagueIdList = leagueIdList,
                                isIncrement = true)
                        }
                    }
                }
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

        gameTypeList.find { it.isSelected }.let { item ->
            game_toolbar_sport_type.text = item?.name ?: resources.getString(GameType.FT.string)
            updateSportBackground(item)
            subscribeSportChannelHall(item?.code)
        }
    }

    private fun updateSportBackground(sport: Item?) {
        when {
            game_bg_layer2.isVisible -> game_bg_layer2
            game_bg_layer3.isVisible -> game_bg_layer3
            else -> null
        }?.let {
            Glide.with(requireContext()).load(
                when (sport?.code) {
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
                    else -> {
                        when {
                            game_bg_layer2.isVisible -> R.drawable.soccer108
                            game_bg_layer3.isVisible -> R.drawable.soccer140
                            else -> null
                        }
                    }
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
            StatusSheetAdapter.ItemCheckedListener { _, playCate ->
                viewModel.switchPlayCategory(args.matchType,play,playCate.code)
                upDateSelectPlay(play)
                (activity as BaseActivity<*>).bottomSheet.dismiss()
                loading()
            })
    }

    //更新isLocked狀態
    private fun upDateSelectPlay(play: Play) {
        val platData = playCategoryAdapter.data.find { it == play }
        if (platData?.selectionType == SelectionType.SELECTABLE.code) {
            platData.isLocked = when {
                platData.isLocked == null || platData.isSelected -> false
                else -> true
            }
        }
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
                args.matchType,
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

    private fun navStatistics(matchId: String?) {
        matchId?.let {
            activity?.apply {
                startActivity(Intent(requireContext(), StatisticsActivity::class.java).apply {
                    putExtra(KEY_MATCH_ID, matchId)
                })

                overridePendingTransition(
                    R.anim.push_bottom_to_top_enter,
                    R.anim.push_bottom_to_top_exit
                )
            }
        }
    }

    private fun addOddsDialog(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateName: String
    ) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        gameType?.let {
            matchInfo?.let { matchInfo ->
                viewModel.updateMatchBetList(
                    args.matchType,
                    gameType,
                    playCateName,
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

    private fun subscribeChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            when (leagueOdd.unfold == FoldState.UNFOLD.code) {
                true -> {
                    subscribeChannelHall(
                        leagueOdd.gameType?.key,
                        getPlayCateMenuCode(),
                        matchOdd.matchInfo?.id
                    )

                    if (matchOdd.matchInfo?.eps == 1) {
                        subscribeChannelHall(
                            leagueOdd.gameType?.key,
                            PlayCate.EPS.value,
                            matchOdd.matchInfo.id
                        )
                    }

                    matchOdd.quickPlayCateList?.forEach {
                        when (it.isSelected) {
                            true -> {
                                subscribeChannelHall(
                                    leagueOdd.gameType?.key,
                                    it.code,
                                    matchOdd.matchInfo?.id
                                )
                            }
                            false -> {
                                unSubscribeChannelHall(
                                    leagueOdd.gameType?.key,
                                    it.code,
                                    matchOdd.matchInfo?.id
                                )
                            }
                        }
                    }
                }

                false -> {
                    unSubscribeChannelHall(
                        leagueOdd.gameType?.key,
                        getPlayCateMenuCode(),
                        matchOdd.matchInfo?.id
                    )

                    if (matchOdd.matchInfo?.eps == 1) {
                        unSubscribeChannelHall(
                            leagueOdd.gameType?.key,
                            PlayCate.EPS.value,
                            matchOdd.matchInfo.id
                        )
                    }

                    matchOdd.quickPlayCateList?.forEach {
                        unSubscribeChannelHall(
                            leagueOdd.gameType?.key,
                            it.code,
                            matchOdd.matchInfo?.id
                        )
                    }
                }
            }
        }
    }

    private fun unSubscribeLeagueChannelHall(leagueOdd: LeagueOdd){
        leagueOdd.matchOdds.forEach {matchOdd ->
            unSubscribeChannelHall(
                leagueOdd.gameType?.key,
                getPlayCateMenuCode(),
                matchOdd.matchInfo?.id
            )

            if (matchOdd.matchInfo?.eps == 1) {
                unSubscribeChannelHall(
                    leagueOdd.gameType?.key,
                    PlayCate.EPS.value,
                    matchOdd.matchInfo.id
                )
            }
        }
    }

    private fun subscribeChannelHall(epsLeagueOddsItem: EpsLeagueOddsItem) {
        val gameType =
            GameType.getGameType(gameTypeAdapter.dataSport.find { item -> item.isSelected }?.code)

        epsLeagueOddsItem.matchOdds?.forEach { matchOddsItem ->
            when (epsLeagueOddsItem.isClose) {
                true -> {
                    unSubscribeChannelHall(
                        gameType?.key,
                        PlayCate.EPS.value,
                        matchOddsItem.matchInfo?.id
                    )
                }
                false -> {
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
        unSubscribeChannelHallSport()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.clearSelectedLeague()

        game_list.adapter = null
    }
}