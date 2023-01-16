package org.cxct.sportlottery.ui.game.hall

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.core.view.isVisible
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import kotlinx.android.synthetic.main.view_match_category_v4.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.common.GameType.Companion.getGameTypeString
import org.cxct.sportlottery.network.league.League
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsLeagueOddsItem
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.network.outright.odds.OutrightShowMoreItem
import org.cxct.sportlottery.network.outright.odds.OutrightSubTitleItem
import org.cxct.sportlottery.network.outright.season.Season
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.EdgeBounceEffectHorizontalFactory
import org.cxct.sportlottery.ui.common.ScrollCenterLayoutManager
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.component.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.common.LeagueAdapter
import org.cxct.sportlottery.ui.game.common.LeagueListener
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.game.hall.adapter.*
import org.cxct.sportlottery.ui.game.outright.OutrightLeagueOddAdapter
import org.cxct.sportlottery.ui.game.outright.OutrightOddListener
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.statistics.StatisticsDialog
import org.cxct.sportlottery.util.*
import java.util.*

/**
 * @app_destination 滾球、即將、今日、早盤、冠軍、串關
 */
class GameV3Fragment : BaseBottomNavigationFragment<GameViewModel>(GameViewModel::class), Animation.AnimationListener {

    private val args: GameV3FragmentArgs by navArgs()
    private var childMatchType = MatchType.OTHER
    private var mView: View? = null
    private var mLeagueIsFiltered = false // 是否套用聯賽過濾
    private var mCalendarSelected = false //紀錄日期圖示選中狀態
    private var isReloadPlayCate: Boolean? = null //是否重新加載玩法篩選Layout

    private val gameTypeAdapter by lazy {
        GameTypeAdapter().apply {
            gameTypeListener = GameTypeListener {
                if (!it.isSelected) {
                    //切換球種，清除日期記憶
                    viewModel.tempDatePosition = 0
                    //日期圖示選取狀態下，切換球種要重置UI狀態
                    if (game_toolbar_calendar.isSelected) game_toolbar_calendar.performClick()
                    (sport_type_list.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
                        sport_type_list,
                        RecyclerView.State(),
                        dataSport.indexOfFirst { item -> TextUtils.equals(it.code, item.code) })
                }

                leagueAdapter.setPreloadItem()
                countryAdapter.setPreloadItem()
                outrightLeagueOddAdapter.setPreloadItem()

                //切換球種後要重置位置
                initMatchCategoryPagerPosition()
                loading()
                isReloadPlayCate = true
                unSubscribeChannelHallAll()
                viewModel.switchGameType(it)
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
            navGameLeague(matchIdList = it.matchList, matchCategoryName = it.categoryDesc)
        })
    }

    private val playCategoryAdapter by lazy {
        PlayCategoryAdapter().apply {
            playCategoryListener = PlayCategoryListener(
                onClickSetItemListener = {
                    viewModel.switchPlay(args.matchType, it)
                },
                onClickNotSelectableListener = {
                    viewModel.switchPlay(args.matchType, it)
                    upDateSelectPlay(it)
                },
                onSelectPlayCateListener = { play, playCate, hasItemSelect ->
                    viewModel.switchPlayCategory(play, playCate.code, hasItemSelect, args.matchType)
                    upDateSelectPlay(play)
                    //當前已選中下拉選單不用重新要資料
                    if (hasItemSelect) {
                        leagueAdapter.data.updateOddsSort(
                            viewModel.getSportSelectedCode(),
                            this
                        )
                        leagueAdapter.updateLeagueByPlayCate()
                    }
                })
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
                    //TODO review此adapter還是否有用
//                    season.id?.let { navGameOutright(it) }
                },
                { leagueId ->
                    viewModel.pinFavorite(FavoriteType.LEAGUE, leagueId)
                }
            )
        }
    }

    private val outrightLeagueOddAdapter by lazy {
        OutrightLeagueOddAdapter().apply {
            outrightOddListener = OutrightOddListener(
                clickListenerBet = { matchOdd, odd, playCateCode ->
                    matchOdd?.let {
                        if (mIsEnabled) {
                            avoidFastDoubleClick()
                            addOutRightOddsDialog(matchOdd, odd, playCateCode)
                        }
                    }
                },
                clickListenerMore = { oddsKey, matchOdd ->
                    matchOdd.oddsMap?.get(oddsKey)?.forEachIndexed { index, odd ->
                        if (index >= 5) {
                            odd?.isExpand?.let { isExpand ->
                                odd.isExpand = !isExpand
                                this.notifyItemChanged(this.data.indexOf(odd))
                            }
                        }
                    }
                },
                clickExpand = { matchOdd, oddsKey ->
                    //TODO 訂閱邏輯需重新調整
                    subscribeChannelHall(matchOdd)

                    matchOdd?.oddsExpand?.get(oddsKey)?.let { oddExpand ->
                        matchOdd.oddsExpand?.put(oddsKey, !oddExpand)
                    }
                    lifecycleScope.launch(Dispatchers.IO) {
                        this@apply.data.filter { any ->
                            //同一場聯賽內的賠率項(Odd)及顯示更多(OutrightShowMoreItem)
                            when (any) {
                                is OutrightShowMoreItem -> {
                                    any.matchOdd == matchOdd
                                }
                                is Odd -> {
                                    any.belongMatchOdd == matchOdd
                                }
                                else -> {
                                    false
                                }
                            }
                        }.forEach { any ->
                            val oddsExpand = matchOdd?.oddsExpand?.get(oddsKey) ?: false

                            when (any) {
                                is OutrightShowMoreItem -> {
                                    if (any.playCateCode == oddsKey) {
                                        if (any.playCateExpand != oddsExpand) {
                                            any.playCateExpand = oddsExpand
                                            updateOutrightAdapterInMain(any)
                                        }
                                    }
                                }
                                is Odd -> {
                                    if (any.outrightCateKey == oddsKey) {
                                        if (any.playCateExpand != oddsExpand) {
                                            any.playCateExpand = oddsExpand
                                            updateOutrightAdapterInMain(any)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                onClickMatch = { matchOdd ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        val newExpanded = !(matchOdd?.isExpanded ?: true)
                        matchOdd?.isExpanded = newExpanded

                        this@apply.data.filter { any ->
                            when (any) {
                                is org.cxct.sportlottery.network.outright.odds.MatchOdd -> {
                                    any == matchOdd
                                }
                                is OutrightSubTitleItem -> {
                                    any.belongMatchOdd == matchOdd
                                }
                                is OutrightShowMoreItem -> {
                                    any.matchOdd == matchOdd
                                }
                                is Odd -> {
                                    any.belongMatchOdd == matchOdd
                                }
                                else -> {
                                    false
                                }
                            }
                        }.forEach { any ->
                            when (any) {
                                is org.cxct.sportlottery.network.outright.odds.MatchOdd -> {
                                    //聯賽標題需更新與其他Item的間隔
                                    updateOutrightAdapterInMain(any)
                                }
                                is OutrightSubTitleItem -> {
                                    if (any.leagueExpanded != newExpanded) {
                                        any.leagueExpanded = newExpanded
                                        updateOutrightAdapterInMain(any)
                                    }
                                }
                                is OutrightShowMoreItem -> {
                                    if (any.leagueExpanded != newExpanded) {
                                        any.leagueExpanded = newExpanded
                                        updateOutrightAdapterInMain(any)
                                    }
                                }
                                is Odd -> {
                                    if (any.leagueExpanded != newExpanded) {
                                        any.leagueExpanded = newExpanded
                                        updateOutrightAdapterInMain(any)
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    private fun updateOutrightAdapterInMain(any: Any) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (game_list.adapter is OutrightLeagueOddAdapter) {
                outrightLeagueOddAdapter.notifyItemChanged(outrightLeagueOddAdapter.data.indexOf(any))
            }
        }
    }

    private val leagueAdapter by lazy {
        LeagueAdapter(
            args.matchType,
            getPlaySelectedCodeSelectionType(),
            getPlaySelectedCode()
        ).apply {
            discount = viewModel.userInfo.value?.discount ?: 1.0F
            leagueListener = LeagueListener {
                if (it.unfoldStatus == FoldState.FOLD.code) {
                    Log.d("[subscribe]", "取消訂閱 ${it.league.name}")
                    unSubscribeChannelHall(it)
                }
                //目前無法監聽收合動畫
                Handler().postDelayed(
                    { game_list?.firstVisibleRange(this, activity ?: requireActivity()) },
                    400
                )
            }
            leagueOddListener = LeagueOddListener(
                clickListenerPlayType = { matchId, matchInfoList, _, liveVideo ->
                    navMatchDetailPage(matchId, matchInfoList, liveVideo)
                },
                clickListenerBet = { view,matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap ->
                    if (mIsEnabled) {
                        avoidFastDoubleClick()
                        addOddsDialog(
                            matchInfo,
                            odd,
                            playCateCode,
                            playCateName,
                            betPlayCateNameMap
                        )
                    }
                },
                clickListenerQuickCateTab = { matchOdd, quickPlayCate ->
                    matchOdd.matchInfo?.let {
                        setQuickPlayCateSelected(matchOdd, quickPlayCate)
                    }
                },
                clickListenerQuickCateClose = {
                    clearQuickPlayCateSelected()
                },
                clickListenerFavorite = { matchId ->
                    matchId?.let {
                        viewModel.pinFavorite(FavoriteType.MATCH, it)
                    }
                },
                clickListenerStatistics = { matchId ->
                    navStatistics(matchId)
                },
                refreshListener = { matchId ->
                    loading()
                    viewModel.refreshGame(
                        args.matchType,
                        listOf(),
                        listOf(matchId)
                    )
                },
                clickLiveIconListener = { matchId, matchInfoList, _, liveVideo ->
                    if (viewModel.checkLoginStatus()) {
                        navMatchDetailPage(matchId, matchInfoList, liveVideo)
                    }
                },
                clickAnimationIconListener = { matchId, matchInfoList, _, liveVideo ->
                    if (viewModel.checkLoginStatus()) {
                        navMatchDetailPage(matchId, matchInfoList, liveVideo)
                    }
                },
                clickCsTabListener = { playCate, matchOdd ->
                    data.forEachIndexed { index, l ->
                        l.matchOdds.find { m ->
                            m == matchOdd
                        }?.let {
                            it.csTabSelected = playCate
                            updateLeagueBySelectCsTab(index, matchOdd)
                        }
                    }
                }
            )
        }
    }

    private fun navMatchDetailPage(matchId: String?, matchInfoList: List<MatchInfo>, liveVideo: Int) {
        matchId?.let {
            navOddsDetailLive(it, liveVideo)
        }
    }

    private val epsListAdapter by lazy {
        EpsListAdapter(EpsListAdapter.EpsOddListener(
            {
                subscribeChannelHall(it)
            },
            { odd, betMatchInfo, betPlayCateNameMap ->
                if (mIsEnabled) {
                    avoidFastDoubleClick()
                    addOddsDialog(
                        betMatchInfo,
                        odd,
                        PlayCate.EPS.value,
                        getString(R.string.game_tab_price_boosts_odd),
                        betPlayCateNameMap
                    )
                }
            }) { matchInfo ->
            setEpsBottomSheet(matchInfo)
        }
        )
    }

    private var isUpdatingLeague = false

    private var mLeagueOddList = ArrayList<LeagueOdd>()
    private var mQuickOddListMap = HashMap<String, MutableList<QuickPlayCate>>()

    private lateinit var moreEpsInfoBottomSheet: BottomSheetDialog

    val gameToolbarMatchTypeText = { matchType: MatchType ->
        when (matchType) {
            MatchType.IN_PLAY -> getString(R.string.home_tab_in_play)
            MatchType.TODAY -> getString(R.string.home_tab_today)
            MatchType.EARLY -> getString(R.string.home_tab_early)
            MatchType.CS -> getString(R.string.home_tab_cs)
            MatchType.PARLAY -> getString(R.string.home_tab_parlay)
            MatchType.AT_START -> getString(R.string.home_tab_at_start_2)
            MatchType.OUTRIGHT -> getString(R.string.home_tab_outright)
            MatchType.EPS -> getString(R.string.premium_odds)
            MatchType.OTHER -> viewModel.specialEntrance.value?.couponName
            else -> ""
        }
    }

    override fun loading() {
        stopTimer()
    }

    override fun hideLoading() {
        if (timer == null) startTimer()
    }

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            countryAdapter.setPreloadItem()
            outrightLeagueOddAdapter.setPreloadItem()
            //切換tab要重置位置
            initMatchCategoryPagerPosition()
            when (tab?.text.toString()) { //固定寫死
                getString(R.string.game_tab_league_odd) -> { //賽事
                    game_toolbar_calendar.visibility =
                        if (args.matchType == MatchType.EARLY || args.matchType == MatchType.CS) View.VISIBLE else View.GONE
                    game_filter_type_list.visibility =
                        if (game_toolbar_calendar.isSelected) View.VISIBLE else View.GONE
                    if (args.matchType == MatchType.OTHER) {
                        game_play_category.visibility = View.VISIBLE
                    }
                    childMatchType = args.matchType
                    viewModel.switchChildMatchType(childMatchType = args.matchType)
                }
                getString(R.string.game_tab_outright_odd) -> { //冠軍
                    game_toolbar_calendar.visibility = View.GONE
                    game_filter_type_list.visibility = View.GONE
                    if (args.matchType == MatchType.OTHER) {
                        game_play_category.visibility = View.GONE
                        childMatchType = MatchType.OTHER_OUTRIGHT
                        viewModel.switchChildMatchType(childMatchType = MatchType.OTHER_OUTRIGHT)
                    } else {
                        viewModel.switchChildMatchType(childMatchType = MatchType.OUTRIGHT)
                    }
                }
//                getString(R.string.game_tab_price_boosts_odd) -> { //特優賠率  需求先隱藏特優賠率
//                    if (args.matchType == MatchType.OTHER) {
//                        viewModel.switchChildMatchType(childMatchType = MatchType.OTHER_EPS)
//                    } else {
//                        viewModel.switchChildMatchType(childMatchType = MatchType.EPS)
//                    }
//                }
            }
            game_match_category_pager.isVisible =
                tab?.text.toString() == getString(R.string.game_tab_league_odd) &&
                        (args.matchType == MatchType.TODAY || args.matchType == MatchType.PARLAY)
                        && matchCategoryPagerAdapter.itemCount > 0
                        && game_tabs.selectedTabPosition == 0
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }
    }

    init {
        afterAnimateListener = AfterAnimateListener {
            try {
//                initObserve()
//                initSocketObserver()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.resetOtherSeelectedGameType()
        mView = inflater.inflate(R.layout.fragment_game_v3, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //若為起始fragment不會有轉場動畫, 故無法透過afterAnimateListener動作
        initObserve()
        initSocketObserver()
    }

    private fun setupSportTypeList() {
        sport_type_list.apply {
            this.layoutManager =
                ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()

            this.adapter = gameTypeAdapter
            removeItemDecorations()
//            addItemDecoration(
//                SpaceItemDecoration(
//                    context,
//                    R.dimen.recyclerview_item_dec_spec_sport_type
//                )
//            )
        }
    }

    private fun setupToolbar() {
        when (args.matchType) {
            MatchType.CS, MatchType.OTHER -> game_toolbar_match_type.text =
                gameToolbarMatchTypeText(args.matchType)
            else -> {
            }
        }

        game_toolbar_champion.apply {
            visibility = when (args.matchType) {
                MatchType.IN_PLAY, MatchType.AT_START -> {
                    if (viewModel.getMatchCount(args.matchType) < 1) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }
                else -> {
                    View.GONE
                }
            }

            setOnClickListener {
                GameType.getGameType(viewModel.getSportSelectedCode(args.matchType))?.let {
                    val action =
                        GameV3FragmentDirections.actionGameV3FragmentToLeagueFilterFragment(
                            it,
                            args.matchType
                        )
                    findNavController().navigate(action)
                }
            }
        }

        game_toolbar_calendar.apply {
            visibility = when (args.matchType) {
                MatchType.EARLY -> View.VISIBLE
                MatchType.CS -> View.VISIBLE
                else -> View.GONE
            }
            setOnClickListener {
                val newSelectedStatus = !isSelected
                mCalendarSelected = newSelectedStatus
                isSelected = newSelectedStatus
                view?.game_filter_type_list?.visibility = when (game_toolbar_calendar.isSelected) {
                    true -> View.VISIBLE
                    false -> View.GONE
                }
            }
        }

        if (args.matchType == MatchType.IN_PLAY) {
            game_toolbar_back.visibility = View.INVISIBLE
        }
        game_toolbar_back.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun refreshToolBarUI() {
        if (view != null) {
            if (leagueAdapter.data.isEmpty()) {
                if (args.matchType == MatchType.AT_START)
                    game_toolbar_champion.isVisible = false
            } else {
                if (args.matchType == MatchType.AT_START)
                    game_toolbar_champion.isVisible = true
            }

            if (args.matchType == MatchType.OTHER) {
                game_toolbar_champion.isVisible = false
            }
        }
    }

    private fun setupOddTab() {
        game_tabs.apply {
            addOnTabSelectedListener(onTabSelectedListener)
        }

        game_tab_odd_v4.visibility = when (args.matchType) {
            MatchType.TODAY, MatchType.EARLY, MatchType.PARLAY, MatchType.OTHER -> {
                if (viewModel.getMatchCount(args.matchType) < 1) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
            else -> View.GONE
        }

        val epsItem = (game_tab_odd_v4.game_tabs.getChildAt(0) as ViewGroup).getChildAt(2)
        if (game_tab_odd_v4.visibility == View.VISIBLE && args.matchType == MatchType.PARLAY) {
            epsItem.visibility = View.GONE
        } else if (args.matchType == MatchType.OTHER || args.matchType == MatchType.OTHER_OUTRIGHT) {
            epsItem.visibility = View.GONE
        } else {
            epsItem.visibility = View.GONE//0401 特優賠率入口隱藏
        }
    }

    private fun setOtherOddTab(isGameListNull: Boolean) {
        val gameItem = (view?.game_tab_odd_v4?.game_tabs?.getChildAt(0) as ViewGroup).getChildAt(0)

        if (isGameListNull) {
            game_tabs.selectTab(game_tabs.getTabAt(1))
        }
        gameItem.isVisible = !isGameListNull
    }

    private fun setupSportBackground() {
        game_bg_layer2.visibility = when (args.matchType) {
            MatchType.IN_PLAY, MatchType.AT_START, MatchType.OUTRIGHT, MatchType.EPS -> View.VISIBLE
            else -> View.GONE
        }

        game_bg_layer3?.visibility = when (args.matchType) {
            MatchType.TODAY, MatchType.EARLY, MatchType.CS, MatchType.PARLAY, MatchType.OTHER -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun setupMatchCategoryPager() {
        match_category_pager.adapter = matchCategoryPagerAdapter
        match_category_pager.getChildAt(0)?.overScrollMode = View.OVER_SCROLL_NEVER //移除漣漪效果
        OverScrollDecoratorHelper.setUpOverScroll(
            match_category_pager.getChildAt(0) as RecyclerView,
            OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL
        )
        match_category_indicator.setupWithViewPager2(match_category_pager)
    }

    private fun setupPlayCategory() {
        game_play_category.apply {
            if (this.layoutManager == null || isReloadPlayCate != false) {
                this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()

            if (this.adapter == null || isReloadPlayCate != false) {
                this.adapter = playCategoryAdapter
            }
            removeItemDecorations()
            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_play_category
                )
            )

            visibility =
                if (args.matchType == MatchType.IN_PLAY || args.matchType == MatchType.AT_START || (args.matchType == MatchType.OTHER && childMatchType == MatchType.OTHER)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
    }

    private fun setupGameRow() {
        game_filter_type_list.apply {
            this.layoutManager =
                ScrollCenterLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            edgeEffectFactory = EdgeBounceEffectHorizontalFactory()

            this.adapter = dateAdapter
            removeItemDecorations()
            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_date
                )
            )
        }

        game_filter_type_list.visibility =
            if ((args.matchType == MatchType.EARLY || args.matchType == MatchType.CS) && mCalendarSelected) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun setupGameListView() {
        game_list.apply {
            this.layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = leagueAdapter
            addScrollWithItemVisibility(
                onScrolling = {
                    unSubscribeChannelHallAll()
                },
                onVisible = {
                    when (adapter) {
                        is LeagueAdapter -> {
                            if (leagueAdapter.data.isNotEmpty()) {
                                it.forEach { p ->
                                    Log.d(
                                        "[subscribe]",
                                        "訂閱 ${leagueAdapter.data[p.first].league.name} -> " +
                                                "${leagueAdapter.data[p.first].matchOdds[p.second].matchInfo?.homeName} vs " +
                                                "${leagueAdapter.data[p.first].matchOdds[p.second].matchInfo?.awayName}"
                                    )
                                    subscribeChannelHall(
                                        leagueAdapter.data[p.first].gameType?.key,
                                        leagueAdapter.data[p.first].matchOdds[p.second].matchInfo?.id
                                    )
                                }
                            }
                        }
                        //冠軍
                        is OutrightLeagueOddAdapter -> {
                            it.forEach { pair ->
                                if (pair.first < outrightLeagueOddAdapter.data.size) {
                                    val outrightDataList = outrightLeagueOddAdapter.data[pair.first]
                                    when (outrightDataList) {
                                        is org.cxct.sportlottery.network.outright.odds.MatchOdd -> {
                                            outrightDataList
                                        }
                                        is OutrightSubTitleItem -> {
                                            outrightDataList.belongMatchOdd
                                        }
                                        is Odd -> {
                                            outrightDataList.belongMatchOdd
                                        }
                                        is OutrightShowMoreItem -> {
                                            outrightDataList.matchOdd
                                        }
                                        else -> {
                                            null
                                        }
                                    }?.let { itemMatchOdd ->
                                        Log.d(
                                            "[subscribe]",
                                            "訂閱 ${itemMatchOdd.matchInfo?.name} -> " +
                                                    "${itemMatchOdd.matchInfo?.homeName} vs " +
                                                    "${itemMatchOdd.matchInfo?.awayName}"
                                        )
                                        subscribeChannelHall(
                                            viewModel.getSportSelectedCode(),
                                            itemMatchOdd.matchInfo?.id
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
            if (viewModel.getMatchCount(args.matchType) < 1) {
                leagueAdapter.removePreloadItem()
            } else {
                leagueAdapter.setPreloadItem()
                countryAdapter.setPreloadItem()
                outrightLeagueOddAdapter.setPreloadItem()
            }
        }
    }

    /**
     * 設置冠軍adapter, 訂閱當前頁面上的資料
     */
    private fun setOutrightLeagueAdapter() {
        if (game_list.adapter !is OutrightLeagueOddAdapter) {
            game_list.adapter = outrightLeagueOddAdapter
        }

        if (game_list.adapter is OutrightLeagueOddAdapter) {
            Handler().postDelayed(
                {
                    game_list?.firstVisibleRange(
                        outrightLeagueOddAdapter,
                        activity ?: requireActivity()
                    )
                },
                400
            )
        }
    }

    private fun initObserve() {
        viewModel.showErrorDialogMsg.observe(this.viewLifecycleOwner) {
            if (it != null && it.isNotBlank()) {
                context?.let { context ->
                    val dialog = CustomAlertDialog(context)
                    dialog.setTitle(resources.getString(R.string.prompt))
                    dialog.setMessage(it)
                    dialog.setTextColor(R.color.color_E44438_e44438)
                    dialog.setNegativeButtonText(null)
                    dialog.setPositiveClickListener {
                        viewModel.resetErrorDialogMsg()
                        dialog.dismiss()
                        back()
                    }
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.isCancelable = false
                    dialog.show(childFragmentManager, null)
                }
            }
        }

        viewModel.sportMenuResult.observe(this.viewLifecycleOwner) {
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

                MatchType.CS -> {
                    updateSportType(it?.sportMenuData?.menu?.cs?.items ?: listOf())
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

                MatchType.OTHER -> {
                    val tempItem: MutableList<Item> = mutableListOf()
                    viewModel.specialMenuData?.items?.forEach { item ->
                        val mItem = Item(
                            code = item.code ?: "",
                            name = item.name ?: "",
                            num = item.num ?: 0,
                            play = null,
                            sortNum = item.sortNum ?: 0,
                        )
                        mItem.hasPlay = (item.play != null)
                        mItem.isSelected = item.isSelected
                        tempItem.add(mItem)
                    }
                    updateSportType(tempItem)
                }

                else -> {
                }
            }
        }

        viewModel.matchCategoryQueryResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.rows?.let { resultList ->
                setMatchCategoryPagerVisibility(resultList.size)
                // TODO view_space_first.isVisible = !isCateShow
                matchCategoryPagerAdapter.data = resultList
            }
        }

        viewModel.curDate.observe(this.viewLifecycleOwner) {
            dateAdapter.data = it
        }

        viewModel.curDatePosition.observe(this.viewLifecycleOwner) {
            var position = viewModel.tempDatePosition
            position = if (position != 0) position else it
            (game_filter_type_list.layoutManager as ScrollCenterLayoutManager?)?.smoothScrollToPosition(
                game_filter_type_list,
                RecyclerView.State(),
                position
            )
        }

        viewModel.curChildMatchType.observe(this.viewLifecycleOwner) {
            //預設第一項
            when (it) {
                null -> {
                    //init tab select
                    game_tabs.clearOnTabSelectedListeners()
                    game_tabs.selectTab(game_tabs.getTabAt(0))
                    game_tabs.addOnTabSelectedListener(onTabSelectedListener)
                }
                MatchType.OTHER_OUTRIGHT -> {
                    game_tabs.selectTab(game_tabs.getTabAt(1))
                }
                MatchType.OUTRIGHT -> {
                    game_tabs.selectTab(game_tabs.getTabAt(1))
                }
                MatchType.OTHER_EPS -> {
                    game_tabs.selectTab(game_tabs.getTabAt(2))
                }
                MatchType.EPS -> {
                    game_tabs.selectTab(game_tabs.getTabAt(2))
                }
                else -> {
                    game_tabs.selectTab(game_tabs.getTabAt(0))
                }
            }
        }

        viewModel.userInfo.observe(this.viewLifecycleOwner) { userInfo ->
            when (game_list.adapter) {
                is LeagueAdapter -> {
                    leagueAdapter.discount = userInfo?.discount ?: 1.0F
                }

                is EpsListAdapter -> {
                    epsListAdapter.discount = userInfo?.discount ?: 1.0F
                }

                is OutrightLeagueOddAdapter -> {
                    viewModel.updateOutrightDiscount(userInfo?.discount ?: 1.0F)
                }
            }
        }

        viewModel.oddsListGameHallResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { oddsListResult ->
                if (oddsListResult.success) {
                    mLeagueOddList.clear()
                    mLeagueOddList.addAll(
                        oddsListResult.oddsListData?.leagueOddsFilter
                            ?: oddsListResult.oddsListData?.leagueOdds ?: listOf()
                    )

                    if (mLeagueOddList.isNotEmpty()) {
                        leagueAdapter.playSelectedCodeSelectionType =
                            getPlaySelectedCodeSelectionType()
                        game_list.layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
                        leagueAdapter.data = mLeagueOddList.onEach { leagueOdd ->
                            // 將儲存的賠率表指定的賽事列表裡面
                            val leagueOddFromMap = leagueOddMap[leagueOdd.league.id]
                            leagueOddFromMap?.let {
                                leagueOdd.matchOdds.forEach { mMatchOdd ->
                                    mMatchOdd.oddsMap =
                                        leagueOddFromMap.matchOdds.find { matchOdd -> mMatchOdd.matchInfo?.id == matchOdd.matchInfo?.id }?.oddsMap
                                }
                            }
                            leagueOdd.gameType = GameType.getGameType(oddsListResult.oddsListData?.sport?.code)
                        }.toMutableList()
                        leagueAdapter.playSelectedCodeSelectionType = getPlaySelectedCodeSelectionType()
                        leagueAdapter.playSelectedCode = getPlaySelectedCode()
                    } else {
                        game_list.layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
                        leagueAdapter.data = mLeagueOddList
                        // Todo: MatchType.OTHER 要顯示無資料與隱藏篩選清單
                    }
                    if (game_list.adapter !is LeagueAdapter) game_list.adapter = leagueAdapter

                    //如果data資料為空時，又有其他球種的情況下，自動選取第一個
                    if (mLeagueOddList.isNullOrEmpty() && gameTypeAdapter.dataSport.size > 1) {
                        if (args.matchType == MatchType.OTHER) {
                            // 待觀察，再決定是否要補
                        } else {
                            viewModel.switchFirstSportType(args.matchType)
                        }
                    }

                    mLeagueOddList.forEach { leagueOdd ->
                        unSubscribeChannelHall(leagueOdd)
                    }

                    oddsListResult.oddsListData?.leagueOdds?.forEach { leagueOdd ->
                        leagueOdd.matchOdds.forEachIndexed { _, matchOdd ->
                            mQuickOddListMap[matchOdd.matchInfo?.id ?: ""] =
                                matchOdd.quickPlayCateList ?: mutableListOf()
                        }
                    }
                    leagueAdapter.limitRefresh()
                    // TODO 這裡要確認是否有其他地方重複呼叫
                    Log.d("Hewie", "observe => OddsListGameHallResult")

                    game_list?.firstVisibleRange(leagueAdapter, activity ?: requireActivity())

                }
                refreshToolBarUI()
            } ?: run {
                leagueAdapter.setPreloadItem()
                countryAdapter.setPreloadItem()
                outrightLeagueOddAdapter.setPreloadItem()
            }
            hideLoading()
        }

        viewModel.oddsListGameHallIncrementResult.observe(this.viewLifecycleOwner) {
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
                                    this.unfoldStatus = targetLeagueOdd.unfoldStatus
                                    this.gameType = targetLeagueOdd.gameType
                                    this.searchMatchOdds = targetLeagueOdd.searchMatchOdds
                                }
                                //更新快捷玩法
                                changedLeagueOdd.matchOdds.forEachIndexed { _, matchOdd ->
                                    mQuickOddListMap[matchOdd.matchInfo?.id ?: ""] =
                                        matchOdd.quickPlayCateList ?: mutableListOf()
                                }
                                subscribeChannelHall(leagueAdapter.data[targetIndex])
                                Log.d("Hewie", "更新聯賽：${targetLeagueOdd.league.name}")
                            } ?: run {
                                Log.d("Hewie", "移除聯賽：${leagueAdapter.data[targetIndex].league.name}")
                                unSubscribeChannelHall(leagueAdapter.data[targetIndex])
                                leagueAdapter.data.removeAt(targetIndex)
                                leagueAdapter.notifyItemRemoved(targetIndex)
                            }
                        } ?: run {
                        //不在畫面上的League
                        val changedLeague =
                            leagueListIncrement?.oddsListData?.leagueOdds?.find { leagueOdd -> leagueOdd.league.id == leagueId }
                        changedLeague?.let { changedLeagueOdd ->
                            val insertLeagueOdd = changedLeagueOdd.apply {
                                this.gameType = GameType.getGameType(leagueListIncrement.oddsListData.sport.code)
                            }
                            //更新快捷玩法
                            changedLeagueOdd.matchOdds.forEachIndexed { _, matchOdd ->
                                mQuickOddListMap[matchOdd.matchInfo?.id ?: ""] =
                                    matchOdd.quickPlayCateList ?: mutableListOf()
                            }
                            leagueAdapter.data.add(insertLeagueOdd)
                            leagueAdapter.notifyItemInserted(leagueAdapter.data.size - 1)
                            subscribeChannelHall(insertLeagueOdd)
                            Log.d("Hewie", "增加聯賽：${insertLeagueOdd.league.name}")
                        }
                    }
                }
            }
            hideLoading()
        }

        viewModel.leagueListResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { leagueListResult ->
                if (game_tab_odd_v4.visibility == View.VISIBLE && game_tabs.selectedTabPosition != 0)
                    return@observe

                if (leagueListResult.success) {
                    val rows = leagueListResult.rows ?: listOf()
                    game_list.apply {
                        adapter = countryAdapter.apply {
                            data = rows
                        }
                    }
                }
            }
            hideLoading()
        }

        viewModel.outrightLeagueListResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { outrightSeasonListResult ->
                if (game_tab_odd_v4.visibility == View.VISIBLE && game_tabs.selectedTabPosition != 1)
                    return@observe

                if (outrightSeasonListResult.success) {
                    val rows = outrightSeasonListResult.rows ?: listOf()
                    game_list.apply {
                        adapter = outrightCountryAdapter.apply {
                            data = rows
                        }
                    }
                }
            }
            hideLoading()
        }

        viewModel.outrightMatchList.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { outrightMatchList ->
                if (game_tab_odd_v4.visibility == View.VISIBLE && game_tabs.selectedTabPosition != 1)
                    return@observe

                outrightLeagueOddAdapter.data = outrightMatchList
                setOutrightLeagueAdapter()
                hideLoading()
            }
        }


        viewModel.epsListResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { epsListResult ->
                if (game_tab_odd_v4.visibility == View.VISIBLE && game_tabs.selectedTabPosition != 2)
                    return@observe

                if (epsListResult.success) {
                    val oddsEpsListDatas = epsListResult.rows
                    val epsLeagueOddsItemList = mutableListOf<EpsLeagueOddsItem>()

                    oddsEpsListDatas.forEachIndexed { indexDate, oddsEpsListData ->
                        val newLeagueOddsItem =
                            EpsLeagueOddsItem(
                                date = oddsEpsListData.date,
                                leagueOdds = null,
                            )
                        epsLeagueOddsItemList.add(newLeagueOddsItem)

                        oddsEpsListData.leagueOdd.forEachIndexed { indexLeague, leagueOdds ->
                            epsLeagueOddsItemList.add(
                                EpsLeagueOddsItem(
                                    date = 0,
                                    leagueOdds = leagueOdds
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
            hideLoading()
        }

        viewModel.countryListSearchResult.observe(this.viewLifecycleOwner) {
            countryAdapter.data = it
            hideLoading()
        }

        viewModel.outrightCountryListSearchResult.observe(this.viewLifecycleOwner) {
            outrightCountryAdapter.data = it
            hideLoading()
        }


        //KK要求，當球類沒有資料時，自動選取第一個有賽事的球種
        viewModel.isNoHistory.observe(this.viewLifecycleOwner) {
            when {
                //當前MatchType有玩法數量，只是目前的球種沒有
                it && curMatchTypeHasMatch() -> {
                    unSubscribeChannelHallAll()
                    if (args.matchType != MatchType.OTHER) {
                        viewModel.switchMatchType(args.matchType)
                    }
                }
            }

            hideLoading()
        }

        //當前玩法無賽事
        viewModel.isNoEvents.distinctUntilChanged().observe(this.viewLifecycleOwner) {
            sport_type_list.isVisible = !it && !isRecommendOutright()
            game_toolbar_sport_type.isVisible = !it
            game_play_category.isVisible =
                (args.matchType == MatchType.IN_PLAY || args.matchType == MatchType.AT_START || (args.matchType == MatchType.OTHER && childMatchType == MatchType.OTHER)) && !it

            game_toolbar_match_type.isVisible = !it
            game_toolbar_calendar.isVisible =
                (args.matchType == MatchType.EARLY || args.matchType == MatchType.CS) && !it
            leagueAdapter.removePreloadItem()
            hideLoading()
        }

        viewModel.betInfoList.observe(this.viewLifecycleOwner) {
            it.peekContent().let { betInfoList ->

                leagueAdapter.betInfoList = betInfoList

                val epsOdds = epsListAdapter.dataList

                epsOdds.forEach { epsLeagueOddsItem ->
                    epsLeagueOddsItem.leagueOdds?.matchOdds?.forEach { matchOddsItem ->
                        matchOddsItem.oddsEps?.eps?.forEach { odd ->
                            odd?.isSelected = betInfoList.any { betInfoListData ->
                                betInfoListData.matchOdd.oddsId == odd?.id
                            }
                        }
                    }
                }
                epsListAdapter.notifyDataSetChanged()

                outrightLeagueOddAdapter.data.filterIsInstance<Odd>().forEach { odd ->
                    val betInfoSelected = betInfoList.any { betInfoListData ->
                        betInfoListData.matchOdd.oddsId == odd.id
                    }
                    if (odd.isSelected != betInfoSelected) {
                        odd.isSelected = betInfoSelected
                        outrightLeagueOddAdapter.notifyItemChanged(outrightLeagueOddAdapter.data.indexOf(odd))
                    }
                }
            }
        }

        viewModel.oddsType.observe(this.viewLifecycleOwner) {
            it?.let { oddsType ->
                leagueAdapter.oddsType = oddsType
                epsListAdapter.oddsType = oddsType
            }
        }

        viewModel.leagueSelectedList.observe(this.viewLifecycleOwner) {
            countryAdapter.apply {
                data.forEach { row ->
                    row.list.forEach { league ->
                        league.isSelected = it.any { it.id == league.id }
                    }
                }

                notifyDataSetChanged()
            }
        }

        viewModel.playList.observe(this.viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                playCategoryAdapter.data = it
                if (isReloadPlayCate != false) {
                    mView?.let {
                        setupPlayCategory()
                        isReloadPlayCate = false
                    }
                }
            }
        }

        viewModel.playCate.observe(this.viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                playCategoryAdapter.apply {
                    data.find { it.isSelected }?.playCateList?.forEachIndexed { _, playCate ->
                        playCate.isSelected = (playCate.code == it)
                    }

                    for (index in data.indices) {
                        notifyItemChanged(index)
                    }
                }
            }
        }

        viewModel.favorLeagueList.observe(this.viewLifecycleOwner) {
            updateLeaguePin(it)
            updateLeaguePinOutright(it)
        }

        viewModel.leagueSubmitList.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { leagueList ->
                navGameLeague(leagueIdList = leagueList.map { league ->
                    league.id
                })
            }
        }

        viewModel.favorMatchList.observe(this.viewLifecycleOwner) {
            leagueAdapter.data.forEach { leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    matchOdd.matchInfo?.isFavorite = it.contains(matchOdd.matchInfo?.id)
                }
            }
            updateAllGameList()
        }

        viewModel.leagueFilterList.observe(this.viewLifecycleOwner) { leagueList ->
            mLeagueIsFiltered = leagueList.isNotEmpty()
            game_toolbar_champion.isSelected = mLeagueIsFiltered
            sport_type_list.visibility =
                if (mLeagueIsFiltered || isRecommendOutright() || args.matchType == MatchType.CS) View.GONE else View.VISIBLE
        }

        viewModel.checkInListFromSocket.observe(this.viewLifecycleOwner) { leagueChangeEvent ->
            if (leagueChangeEvent != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    if (!isUpdatingLeague) {
                        isUpdatingLeague = true

                        //收到的gameType与用户当前页面所选球种相同, 则需额外调用/match/odds/simple/list & /match/odds/eps/list
                        if (leagueAdapter.data.isNotEmpty()) {
                            if (viewModel.getSportSelectedCode() == leagueChangeEvent.gameType && leagueChangeEvent.leagueIdList?.isNotEmpty() == true) {
                                //不管是否相同聯賽，也要確認是否要更新賽事資訊
                                withContext(Dispatchers.Main) {
                                    if (args.matchType == MatchType.OTHER) {
                                        viewModel.getAllPlayCategoryBySpecialMatchType(isReload = false)
                                    } else {
                                        viewModel.getGameHallList(
                                            args.matchType, leagueIdList = leagueChangeEvent.leagueIdList,
                                            isReloadDate = false,
                                            isIncrement = true
                                        )
                                    }
                                }
                            } else if (args.matchType == MatchType.OTHER) {
                                viewModel.getAllPlayCategoryBySpecialMatchType(isReload = false)
                            }
                        }
                        isUpdatingLeague = false
                    }
                }
            }
        }
    }

    /**
     * 判斷當前MatchType是否有玩法數量
     */
    private fun curMatchTypeHasMatch(): Boolean {
        return when (args.matchType) {
            MatchType.IN_PLAY -> viewModel.sportMenuResult.value?.sportMenuData?.menu?.inPlay?.num ?: 0 > 0
            MatchType.TODAY -> viewModel.sportMenuResult.value?.sportMenuData?.menu?.today?.num ?: 0 > 0
            MatchType.AT_START -> viewModel.sportMenuResult.value?.sportMenuData?.atStart?.num ?: 0 > 0
            MatchType.EARLY -> viewModel.sportMenuResult.value?.sportMenuData?.menu?.early?.num ?: 0 > 0
            MatchType.CS -> viewModel.sportMenuResult.value?.sportMenuData?.menu?.cs?.num ?: 0 > 0
            MatchType.PARLAY -> viewModel.sportMenuResult.value?.sportMenuData?.menu?.parlay?.num ?: 0 > 0
            MatchType.OUTRIGHT -> viewModel.sportMenuResult.value?.sportMenuData?.menu?.outright?.num ?: 0 > 0
            MatchType.EPS -> viewModel.sportMenuResult.value?.sportMenuData?.menu?.eps?.num ?: 0 > 0
            MatchType.OTHER -> viewModel.specialMenuData?.items?.size ?: 0 > 0
            else -> false
        }
    }

    private fun updateLeaguePin(leagueListPin: List<String>) {
        val leaguePinList = mutableListOf<League>()

        countryAdapter.data.forEachIndexed { index, row ->
            val pinLeague = row.list.filter { league ->
                leagueListPin.contains(league.id)
            }

            var needUpdate = false

            row.list.forEach { league ->
                if (league.isPin != leagueListPin.contains(league.id)) {
                    league.isPin = leagueListPin.contains(league.id)
                    needUpdate = true
                }
            }

            if (needUpdate)
                countryAdapter.notifyCountryItem(index)

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

    private val leagueOddMap = HashMap<String, LeagueOdd>()
    private fun initSocketObserver() {
        receiver.serviceConnectStatus.observe(this.viewLifecycleOwner) {
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
                    viewModel.firstSwitchMatch(matchType = args.matchType)
                    if (args.matchType == MatchType.OTHER) {
                        viewModel.getAllPlayCategoryBySpecialMatchType(isReload = true)
                    } else if (!args.gameType.isNullOrEmpty() && args.matchType == MatchType.OUTRIGHT && isRecommendOutright()) {
                        args.gameType?.let { gameType ->
                            viewModel.getOutrightOddsList(gameType = gameType, outrightLeagueId = args.outrightLeagueId)
                        }
                    } else {
                        viewModel.getGameHallList(
                            matchType = args.matchType,
                            isReloadDate = true,
                            isReloadPlayCate = (isReloadPlayCate != false),
                            isLastSportType = true
                        )
                    }
                    viewModel.getMatchCategoryQuery(args.matchType)
                    subscribeSportChannelHall()
                } else {
                    stopTimer()
                }
            }
        }

        receiver.matchStatusChange.observe(this.viewLifecycleOwner) {
            it?.let { matchStatusChangeEvent ->
                when (game_list.adapter) {
                    is LeagueAdapter -> {

                        leagueAdapter.data.toList().forEachIndexed { index, leagueOdd ->
                            if (matchStatusChangeEvent.matchStatusCO?.status == GameMatchStatus.FINISH.value) {
                                leagueOdd.matchOdds.find { m ->
                                    m.matchInfo?.id == matchStatusChangeEvent.matchStatusCO.matchId
                                }?.let { mo ->
                                    leagueOdd.matchOdds.remove(mo)
                                    if (leagueOdd.matchOdds.size > 0) {
                                        leagueAdapter.notifyItemChanged(index)
                                    } else {
                                        unSubscribeChannelHall(leagueOdd)
                                        leagueAdapter.data.remove(leagueOdd)
                                        leagueAdapter.notifyItemRemoved(index)
                                    }
                                }
                            }
                        }

                        val leagueOdds = leagueAdapter.data

                        leagueOdds.forEachIndexed { index, leagueOdd ->
                            if (SocketUpdateUtil.updateMatchStatus(
                                    viewModel.getSportSelectedCode(),
                                    leagueOdd.matchOdds.toMutableList(),
                                    matchStatusChangeEvent,
                                    context
                                ) &&
                                leagueOdd.unfoldStatus == FoldState.UNFOLD.code
                            ) {
                                if (leagueOdd.matchOdds.isNullOrEmpty()) {
                                    unSubscribeChannelHall(leagueOdd)
                                    leagueAdapter.data.remove(leagueOdd)
                                    leagueAdapter.notifyItemRemoved(index)
                                }
                            }
                        }
                    }
                }
            }
        }

        receiver.matchClock.observe(this.viewLifecycleOwner) {
            it?.let { matchClockEvent ->
                when (game_list.adapter) {
                    is LeagueAdapter -> {
                        leagueAdapter.data.forEachIndexed { _, leagueOdd ->
                            leagueOdd.matchOdds.forEach { matchOdd ->
                                SocketUpdateUtil.updateMatchClock(matchOdd, matchClockEvent)
                            }
                        }
                    }
                }
            }
        }

        receiver.oddsChangeListener = ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
            when (game_list?.adapter) {
                is LeagueAdapter -> {
                    leagueAdapter.data.forEach { leagueOdd ->
                        leagueOdd.matchOdds.forEach { matchOdd ->
                            matchOdd.quickPlayCateList =
                                mQuickOddListMap[matchOdd.matchInfo?.id]
                        }
                    }

                    val leagueOdds = leagueAdapter.data
                    leagueOdds.sortOddsMap()

                    //MatchType為波膽時, 不透過playCategory選中狀態進行更新, 於下方翻譯更新直接更新
                    if (args.matchType != MatchType.CS) {
                        leagueOdds.updateOddsSort(
                            viewModel.getSportSelectedCode(),
                            playCategoryAdapter
                        ) //篩選玩法
                    }

                    //翻譯更新
                    leagueOdds.forEach { LeagueOdd ->
                        //波膽 玩法名稱翻譯更新
                        if (args.matchType == MatchType.CS) {
                            LeagueOdd.playCateNameMap = PlayCateMenuFilterUtils.filterPlayCateNameMap(GameType.FT.name, PlayCate.CS.value)
                        }
                        LeagueOdd.matchOdds.forEach { MatchOdd ->
                            if (MatchOdd.matchInfo?.id == oddsChangeEvent.eventId) {
                                //馬克說betPlayCateNameMap還是由socket更新
                                oddsChangeEvent.betPlayCateNameMap?.let {
                                    MatchOdd.betPlayCateNameMap?.putAll(it)
                                }
                            }
                        }
                    }

                    leagueAdapter.playSelectedCodeSelectionType = getPlaySelectedCodeSelectionType()
                    leagueAdapter.playSelectedCode = getPlaySelectedCode()

                    leagueOdds.forEachIndexed { index, leagueOdd ->
                        if (leagueOdd.matchOdds.any { matchOdd ->
                                SocketUpdateUtil.updateMatchOdds(
                                    context, matchOdd, oddsChangeEvent, args.matchType
                                )
                            } &&
                            leagueOdd.unfoldStatus == FoldState.UNFOLD.code
                        ) {
                            leagueOddMap[leagueOdd.league.id] = leagueOdd
                            updateBetInfo(leagueOdd, oddsChangeEvent)
                        }
                        leagueAdapter.data[index] = leagueOdd
                        if (game_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !game_list.isComputingLayout) {
                            leagueAdapter.updateLeagueByPosition(oddsChangeEvent.eventId)
                        }
                    }
                }

                is EpsListAdapter -> {
                    val epsOdds = epsListAdapter.dataList

                    epsOdds.forEachIndexed { index, leagueOdd ->
                        if (leagueOdd.leagueOdds?.matchOdds?.any { matchOdd ->
                                SocketUpdateUtil.updateMatchOdds(
                                    context, matchOdd, oddsChangeEvent, args.matchType
                                )
                            } == true && !leagueOdd.isClose) {
                            updateBetInfo(leagueOdd, oddsChangeEvent)
                            epsListAdapter.notifyItemChanged(index)
                        }
                    }
                }

                is OutrightLeagueOddAdapter -> {
                    viewModel.updateOutrightOddsChange(context, oddsChangeEvent)
                }
            }
        }

        receiver.matchOddsLock.observe(this.viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->
                when (game_list.adapter) {
                    is LeagueAdapter -> {
                        leagueAdapter.data.forEachIndexed { _, leagueOdd ->
                            leagueOdd.matchOdds.forEach { matchOdd ->
                                SocketUpdateUtil.updateOddStatus(matchOdd, matchOddsLockEvent)
                            }
                        }
                    }
                    is EpsListAdapter -> {
                        val epsOdds = epsListAdapter.dataList

                        epsOdds.forEachIndexed { index, leagueOdd ->
                            if (leagueOdd.leagueOdds?.matchOdds?.any { matchOdd ->
                                    SocketUpdateUtil.updateOddStatus(matchOdd, matchOddsLockEvent)
                                } == true && !leagueOdd.isClose) {
                                epsListAdapter.notifyItemChanged(index)
                            }
                        }
                    }
                }
            }
        }

        receiver.globalStop.observe(this.viewLifecycleOwner) {
            it?.let { globalStopEvent ->

                when (game_list.adapter) {
                    is LeagueAdapter -> {
                        leagueAdapter.data.forEachIndexed { _, leagueOdd ->
                            leagueOdd.matchOdds.forEach { matchOdd ->
                                SocketUpdateUtil.updateOddStatus(matchOdd, globalStopEvent)
                            }
                        }
                    }

                    is EpsListAdapter -> {
                        val epsOdds = epsListAdapter.dataList

                        epsOdds.forEachIndexed { index, epsLeagueOddsItem ->
                            if (epsLeagueOddsItem.leagueOdds?.matchOdds?.any { matchOdd ->
                                    SocketUpdateUtil.updateOddStatus(
                                        matchOdd,
                                        globalStopEvent
                                    )
                                } == true && !epsLeagueOddsItem.isClose) {
                                epsListAdapter.notifyItemChanged(index)
                            }
                        }
                    }
                }
            }
        }

        receiver.producerUp.observe(this.viewLifecycleOwner) {
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
        }

        //distinctUntilChanged -> 短時間內收到相同leagueChangeEvent僅會執行一次
        receiver.leagueChange.distinctUntilChanged().observe(this.viewLifecycleOwner) {
            it?.let { leagueChangeEvent ->
                viewModel.checkGameInList(
                    matchType = args.matchType,
                    leagueChangeEvent = leagueChangeEvent,
                )
                //待優化: 應有個暫存leagueChangeEvent的機制，確認後續流程更新完畢，再處理下一筆leagueChangeEvent，不過目前後續操作並非都是suspend，需重構後續流程
            }
        }

        receiver.closePlayCate.observe(this.viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                if (viewModel.getSportSelectedCode() != it.gameType) return@observe
                leagueAdapter.data.closePlayCate(it)
                leagueAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * 若投注單處於未開啟狀態且有加入注單的賠率項資訊有變動時, 更新投注單內資訊
     */
    private fun updateBetInfo(leagueOdd: LeagueOdd, oddsChangeEvent: OddsChangeEvent) {
        if (!getBetListPageVisible()) {
            //尋找是否有加入注單的賠率項
            if (leagueOdd.matchOdds.filter { matchOdd ->
                    matchOdd.matchInfo?.id == oddsChangeEvent.eventId
                }.any { matchOdd ->
                    matchOdd.oddsMap?.values?.any { oddList ->
                        oddList?.any { odd ->
                            odd?.isSelected == true
                        } == true
                    } == true
                }) {
                viewModel.updateMatchOdd(oddsChangeEvent)
            }
        }
    }

    private fun updateBetInfo(epsLeagueOddsItem: EpsLeagueOddsItem, oddsChangeEvent: OddsChangeEvent) {
        if (!getBetListPageVisible()) {
            //尋找是否有加入注單的賠率項
            if (epsLeagueOddsItem.leagueOdds?.matchOdds?.filter { matchOddsItem -> matchOddsItem.matchInfo?.id == oddsChangeEvent.eventId }
                    ?.any { matchOdd ->
                        matchOdd.oddsMap?.values?.any { oddList ->
                            oddList?.any { odd ->
                                odd?.isSelected == true
                            } == true
                        } == true
                    } == true) {
                viewModel.updateMatchOdd(oddsChangeEvent)
            }
        }
    }

    private fun updateAllGameList() {
        if (game_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !game_list.isComputingLayout) {
            leagueAdapter.data.forEachIndexed { index, leagueOdd -> leagueAdapter.updateLeague(index, leagueOdd) }
        }
    }

    private fun MutableList<LeagueOdd>.sortOddsMap() {
        this.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                matchOdd.oddsMap?.sortOddsMap()
            }
        }
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
        if (args.matchType == MatchType.CS) {
            //波膽只有FT
            game_toolbar_bg?.setBackgroundResource(R.drawable.img_home_title_soccer_background)
            ll_sport_type.visibility = View.GONE
            return
        } else {
            ll_sport_type.visibility = View.VISIBLE
        }
        gameTypeAdapter.dataSport = gameTypeList

        //post待view繪製完成
        sport_type_list?.post {
            //球種如果選過，下次回來也需要滑動置中
            if (!gameTypeList.isNullOrEmpty()) {
                (sport_type_list?.layoutManager as ScrollCenterLayoutManager?)?.smoothScrollToPosition(
                    sport_type_list,
                    RecyclerView.State(),
                    gameTypeList.indexOfFirst { item -> item.isSelected }
                )
            }

            if (args.matchType != MatchType.OTHER) {
                if (isRecommendOutright()) {
                    args.gameType?.let { gameType ->
                        game_toolbar_sport_type?.text = context?.let {
                            getGameTypeString(it, gameType)
                        } ?: resources.getString(
                            GameType.FT.string
                        ).toUpperCase(Locale.getDefault())
                        if (game_bg_layer2 != null && game_bg_layer3 != null) updateSportBackground(gameType)
                    }
                } else {
                    gameTypeList.find { it.isSelected }.let { item ->
                        game_toolbar_sport_type?.text =
                            context?.let { getGameTypeString(it, item?.code) } ?: resources.getString(
                                GameType.FT.string
                            )
                                .toUpperCase(Locale.getDefault())
                        updateSportBackground(item)
                    }
                }
            } else {
                gameTypeList.find { it.isSelected }.let { item ->
                    item?.let {
                        setOtherOddTab(!it.hasPlay)
                        updateSportBackground(it)
                    }
                }
            }

            if (gameTypeList.isEmpty()) {
                sport_type_list?.visibility = View.GONE
                game_toolbar_sport_type?.visibility = View.GONE
                game_toolbar_champion?.visibility = View.GONE
                game_toolbar_calendar?.visibility = View.GONE
                game_tab_odd_v4?.visibility = View.GONE
                game_match_category_pager?.visibility = View.GONE
                game_play_category?.visibility = View.GONE
                game_filter_type_list?.visibility = View.GONE
            } else {
                sport_type_list?.visibility = if (mLeagueIsFiltered || isRecommendOutright()) View.GONE else View.VISIBLE
                game_toolbar_sport_type?.visibility = View.VISIBLE
                game_toolbar_calendar?.apply {
                        visibility = when (args.matchType) {
                            MatchType.EARLY, MatchType.CS -> View.VISIBLE
                            else -> View.GONE
                        }
                        isSelected = mCalendarSelected
                }

                game_play_category?.visibility = if (args.matchType == MatchType.IN_PLAY || args.matchType == MatchType.AT_START ||
                    (args.matchType == MatchType.OTHER && childMatchType == MatchType.OTHER)
                ) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    private fun updateSportBackground(sport: Item?) {
        if (game_bg_layer2 != null && game_bg_layer3 != null) updateSportBackground(sport?.code)
    }

    private fun updateSportBackground(gameTypeKey: String?) {
        when {
            game_bg_layer2.isVisible -> game_bg_layer2
            game_bg_layer3.isVisible -> game_bg_layer3
            else -> null
        }?.let {
            if (MultiLanguagesApplication.isNightMode) {
                Glide.with(requireContext()).load(
                    when {
                        game_bg_layer2.isVisible -> R.drawable.night_bg_300
                        game_bg_layer3.isVisible -> R.drawable.night_bg_300
                        else -> null
                    }
                ).into(it)
            } else {
                Glide.with(requireContext()).load(
                    when (gameTypeKey) {
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
                        GameType.BM.key -> {
                            when {
                                game_bg_layer2.isVisible -> R.drawable.badminton_100
                                game_bg_layer3.isVisible -> R.drawable.badminton_132
                                else -> null
                            }
                        }
                        GameType.TT.key -> {
                            when {
                                game_bg_layer2.isVisible -> R.drawable.pingpong_100
                                game_bg_layer3.isVisible -> R.drawable.pingpong_140
                                else -> null
                            }
                        }
                        GameType.BX.key -> {
                            when {
                                game_bg_layer2.isVisible -> R.drawable.boxing_100
                                game_bg_layer3.isVisible -> R.drawable.boxing_132
                                else -> null
                            }
                        }
                        GameType.CB.key -> {
                            when {
                                game_bg_layer2.isVisible -> R.drawable.snooker_100
                                game_bg_layer3.isVisible -> R.drawable.snooker_140
                                else -> null
                            }
                        }
                        GameType.CK.key -> {
                            when {
                                game_bg_layer2.isVisible -> R.drawable.cricket_100
                                game_bg_layer3.isVisible -> R.drawable.cricket_132
                                else -> null
                            }
                        }
                        GameType.BB.key -> {
                            when {
                                game_bg_layer2.isVisible -> R.drawable.baseball_100
                                game_bg_layer3.isVisible -> R.drawable.baseball_132
                                else -> null
                            }
                        }
                        GameType.RB.key -> {
                            when {
                                game_bg_layer2.isVisible -> R.drawable.rugby_100
                                game_bg_layer3.isVisible -> R.drawable.rugby_140
                                else -> null
                            }
                        }
                        GameType.AFT.key -> {
                            when {
                                game_bg_layer2.isVisible -> R.drawable.amfootball_100
                                game_bg_layer3.isVisible -> R.drawable.amfootball_132
                                else -> null
                            }
                        }
                        GameType.MR.key -> {
                            when {
                                game_bg_layer2.isVisible -> R.drawable.rancing_100
                                game_bg_layer3.isVisible -> R.drawable.rancing_140
                                else -> null
                            }
                        }
                        GameType.GF.key -> {
                            when {
                                game_bg_layer2.isVisible -> R.drawable.golf_108
                                game_bg_layer3.isVisible -> R.drawable.golf_132
                                else -> null
                            }
                        }
                        GameType.ES.key -> {
                            when {
                                game_bg_layer2.isVisible -> R.drawable.esport_100
                                game_bg_layer3.isVisible -> R.drawable.esport_132
                                else -> null
                            }
                        }
                        GameType.IH.key -> {
                            when {
                                game_bg_layer2.isVisible -> R.drawable.icehockey_100
                                game_bg_layer3.isVisible -> R.drawable.icehockey_140
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
        matchIdList: List<String> = listOf(),
        matchCategoryName: String? = null
    ) {
        val gameType = GameType.getGameType(viewModel.getSportSelectedCode())

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
                matchIdList.toTypedArray(),
                matchCategoryName
            )

            findNavController().navigate(action)
        }
    }

    private fun navOddsDetailLive(matchId: String, liveVideo: Int) {
        val gameType = when (args.matchType) {
            MatchType.CS -> {
                GameType.FT
            }
            else -> {
                GameType.getGameType(viewModel.getSportSelectedCode())
            }
        }

        gameType?.let {
            val action = GameV3FragmentDirections.actionGameV3FragmentToOddsDetailLiveFragment(
                args.matchType,
                gameType,
                matchId,
                liveVideo
            )

            findNavController().navigate(action)
        }
    }

    private fun navStatistics(matchId: String?) {
        StatisticsDialog.newInstance(matchId, StatisticsDialog.StatisticsClickListener { clickMenu() })
            .show(childFragmentManager, StatisticsDialog::class.java.simpleName)
    }

    private fun addOutRightOddsDialog(
        matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd,
        odd: Odd,
        playCateCode: String
    ) {
        val gameType = GameType.getGameType(viewModel.getSportSelectedCode())
        gameType?.let {
            val fastBetDataBean = FastBetDataBean(
                matchType = MatchType.OUTRIGHT,
                gameType = it,
                playCateCode = playCateCode,
                playCateName = "",
                matchInfo = matchOdd.matchInfo!!,
                matchOdd = matchOdd,
                odd = odd,
                subscribeChannelType = ChannelType.HALL,
                betPlayCateNameMap = null,
            )
            (activity as GameActivity).setupBetData(fastBetDataBean)
        }

    }

    private fun addOddsDialog(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    ) {
        val gameType = when (args.matchType) {
            MatchType.CS -> {
                GameType.FT
            }
            else -> {
                GameType.getGameType(viewModel.getSportSelectedCode())
            }
        }

        gameType?.let {
            matchInfo?.let { matchInfo ->
                val fastBetDataBean = FastBetDataBean(
                    matchType = args.matchType,
                    gameType = gameType,
                    playCateCode = playCateCode,
                    playCateName = playCateName,
                    matchInfo = matchInfo,
                    matchOdd = null,
                    odd = odd,
                    subscribeChannelType = ChannelType.HALL,
                    betPlayCateNameMap = betPlayCateNameMap,
                    getPlayCateMenuCode()
                )
                (activity as GameActivity).setupBetData(fastBetDataBean)
            }
        }
    }

    /**
     * 取得當前篩選玩法是否可下拉
     * */
    private fun getPlaySelectedCodeSelectionType(): Int? {
        return playCategoryAdapter.data.find { it.isSelected }?.selectionType
    }

    private fun getPlaySelectedCode(): String? {
        return playCategoryAdapter.data.find { it.isSelected }?.code
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


    private fun subscribeChannelHall(matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd?) {
        val gameType = GameType.getGameType(viewModel.getSportSelectedCode())
        gameType?.let {
            subscribeChannelHall(
                it.key,
                matchOdd?.matchInfo?.id
            )
        }
    }

    private fun subscribeChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            when (leagueOdd.unfoldStatus == FoldState.UNFOLD.code) {
                true -> {
                    subscribeChannelHall(
                        leagueOdd.gameType?.key,
                        matchOdd.matchInfo?.id
                    )
                }

                false -> {
                    unSubscribeChannelHall(
                        leagueOdd.gameType?.key,
                        matchOdd.matchInfo?.id
                    )
                }
            }
        }
    }

    private fun unSubscribeChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            when (leagueOdd.unfoldStatus == FoldState.UNFOLD.code) {
                true -> {
                    unSubscribeChannelHall(
                        leagueOdd.gameType?.key,
                        matchOdd.matchInfo?.id
                    )

                    matchOdd.quickPlayCateList?.forEach {
                        when (it.isSelected) {
                            true -> {
                                unSubscribeChannelHall(
                                    leagueOdd.gameType?.key,
                                    matchOdd.matchInfo?.id
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun unSubscribeLeagueChannelHall(leagueOdd: LeagueOdd) {
        leagueOdd.matchOdds.forEach { matchOdd ->
            unSubscribeChannelHall(
                leagueOdd.gameType?.key,
                matchOdd.matchInfo?.id
            )
        }
    }

    private fun subscribeChannelHall(epsLeagueOddsItem: EpsLeagueOddsItem) {
        val gameType = GameType.getGameType(viewModel.getSportSelectedCode())

        epsLeagueOddsItem.leagueOdds?.matchOdds?.forEach { matchOddsItem ->
            when (epsLeagueOddsItem.isClose) {
                true -> {
                    unSubscribeChannelHall(
                        gameType?.key,
                        matchOddsItem.matchInfo?.id
                    )
                }
                false -> {
                    subscribeChannelHall(
                        gameType?.key,
                        matchOddsItem.matchInfo?.id
                    )
                }
            }
        }
    }

    private var timer: Timer? = null

    private fun startTimer() {
        stopTimer()
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                viewModel.setCurMatchType(null) //避免args.matchType和curMatchType相同，導致後續流程中斷的問題
                viewModel.switchMatchType(args.matchType)
            }
        }, 60 * 3 * 1000L, 60 * 3 * 1000L)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    override fun onStop() {

        super.onStop()
        viewModel.clearSelectedLeague()
        game_list.adapter = null
        stopTimer()
        unSubscribeChannelHallAll()
        unSubscribeChannelHallSport()
    }

    override fun onResume() {
        super.onResume()
        mView?.let {
            setupSportTypeList()
            setupToolbar()
            setupOddTab()
            setupSportBackground()
            setupMatchCategoryPager()
            setupPlayCategory()
            setupGameRow()
            setupGameListView()
            //從內頁返回後要重置位置
            initMatchCategoryPagerPosition()
        }

        if (MultiLanguagesApplication.colorModeChanging) {
            initObserve()
            initSocketObserver()
            MultiLanguagesApplication.colorModeChanging = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearSelectedLeague()
        isReloadPlayCate = null
        game_list.adapter = null
        stopTimer()
        unSubscribeChannelHallAll()
        unSubscribeChannelHallSport()
    }

    // region handle LeagueOdd data
    private fun clearQuickPlayCateSelected() {
        mLeagueOddList.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                matchOdd.isExpand = false
                matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                    quickPlayCate.isSelected = false
                }
            }
        }
    }

    private fun setQuickPlayCateSelected(
        selectedMatchOdd: MatchOdd,
        selectedQuickPlayCate: QuickPlayCate
    ) {
        mLeagueOddList.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                if (selectedMatchOdd.matchInfo?.id == matchOdd.matchInfo?.id) {
                    matchOdd.isExpand = true
                    matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                        if (selectedQuickPlayCate.code == quickPlayCate.code) quickPlayCate.isSelected = true
                    }
                }
            }
        }
    }

    /**
     * @param itemCount 固定為 match category 數量
     **/
    private fun setMatchCategoryPagerVisibility(itemCount: Int) {
        game_match_category_pager.visibility =
            if ((args.matchType == MatchType.TODAY || args.matchType == MatchType.PARLAY) && itemCount != 0) {
                View.VISIBLE
            } else {
                View.GONE
            }
        //賽事選單，一個不要顯示，多個要顯示。
        match_category_indicator.visibility = if (itemCount > 1) View.VISIBLE else View.GONE
    }

    /**
     *  1. 切換球種後要重置位置
     *  2. 切換tab要重置位置
     *  3. 從內頁返回後要重置位置
     */
    private fun initMatchCategoryPagerPosition() {
        match_category_pager.currentItem = 0
    }
    // endregion

    /**
     * 判斷是不是從主頁推薦賽事跳轉至此頁的
     */
    private fun isRecommendOutright(): Boolean {
        return !args.outrightLeagueId.isNullOrEmpty()
    }
}