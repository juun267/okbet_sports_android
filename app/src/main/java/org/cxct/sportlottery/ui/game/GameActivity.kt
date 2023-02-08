package org.cxct.sportlottery.ui.game

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.bottom_navigation_item.view.*
import kotlinx.android.synthetic.main.content_bet_info_item_v3.view.*
import kotlinx.android.synthetic.main.home_cate_tab.view.*
import kotlinx.android.synthetic.main.sport_bottom_navigation.*
import kotlinx.android.synthetic.main.view_bottom_navigation_sport.*
import kotlinx.android.synthetic.main.view_game_tab_match_type_v4.*
import kotlinx.android.synthetic.main.view_message.*
import kotlinx.android.synthetic.main.view_nav_right.*
import kotlinx.android.synthetic.main.view_toolbar_main.*
import kotlinx.coroutines.DelicateCoroutinesApi
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.message.Row
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.MarqueeAdapter
import org.cxct.sportlottery.ui.base.BaseBottomNavActivity
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.component.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.ui.game.betList.BetListFragment
import org.cxct.sportlottery.ui.game.filter.LeagueFilterFragmentDirections
import org.cxct.sportlottery.ui.game.hall.GameV3Fragment
import org.cxct.sportlottery.ui.game.hall.GameV3FragmentDirections
import org.cxct.sportlottery.ui.game.home.HomeFragmentDirections
import org.cxct.sportlottery.ui.game.language.SwitchLanguageActivity
import org.cxct.sportlottery.ui.game.language.SwitchLanguageFragment
import org.cxct.sportlottery.ui.game.league.GameLeagueFragmentDirections
import org.cxct.sportlottery.ui.game.menu.LeftMenuFragment
import org.cxct.sportlottery.ui.game.outright.GameOutrightMoreFragmentDirections
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.game.publicity.PublicitySportEntrance
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterOkActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.MainActivity.Companion.ARGS_THIRD_GAME_CATE
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.main.news.NewsDialog
import org.cxct.sportlottery.ui.menu.ChangeLanguageDialog
import org.cxct.sportlottery.ui.menu.ChangeOddsTypeDialog
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.news.NewsActivity
import org.cxct.sportlottery.ui.odds.OddsDetailLiveFragmentDirections
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ExpandCheckListManager.expandCheckList
import timber.log.Timber
import java.util.*


class GameActivity : BaseBottomNavActivity<GameViewModel>(GameViewModel::class) {

    private val startMatchType = MatchType.IN_PLAY

    companion object {
        fun reStart(context: Context) {
            val intent = Intent(context, GameActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(
                    R.anim.push_right_to_left_enter,
                    R.anim.push_right_to_left_exit
                )
            }
        }

        const val ARGS_SWITCH_LANGUAGE = "switch_language"
        const val ARGS_PUBLICITY_SPORT_ENTRANCE = "publicity_sport_entrance"
    }

    private var betListFragment = BetListFragment()

    private val mMarqueeAdapter by lazy { MarqueeAdapter() }
    private val mNavController by lazy { findNavController(R.id.game_container) }
    private val navDestListener by lazy {
        NavController.OnDestinationChangedListener { _, destination, arguments ->
            updateServiceButtonVisibility(destinationId = destination.id)
            mOutrightLeagueId = arguments?.get("outrightLeagueId") as? String
            when (destination.id) {
                R.id.homeFragment -> {
                    updateSelectTabState(0)
                }

                R.id.gameV3Fragment -> {
                    updateSelectTabState(arguments?.get("matchType") as MatchType)
                }

                R.id.gameLeagueFragment -> {
                    updateSelectTabState(arguments?.get("matchType") as MatchType)
                }
                R.id.oddsDetailLiveFragment -> {
                    //20220504 跟進h5進賽事詳情時不切換至對應的賽事類別
                    // updateSelectTabState(MatchType.IN_PLAY)
                }
            }
        }
    }
    var isFromPublicity: Boolean = false

    private var mOutrightLeagueId: String? = null //主頁跳轉冠軍頁時傳遞的聯賽Id

    private fun updateServiceButtonVisibility(destinationId: Int) {
        when (destinationId) {
            R.id.homeFragment -> {
                btn_floating_service.setView(this)
            }
            else -> {
                btn_floating_service.visibility = View.GONE
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        isFromPublicity = intent.getBooleanExtra(GamePublicityActivity.IS_FROM_PUBLICITY, false)
        setupNoticeButton(iv_notice)
        initToolBar()
        initMenu()
        initSubmitBtn()
        initBottomNavigation()
        initRvMarquee()
        initTabLayout()
        initObserve()
        initServiceButton()

        try {
            val flag = intent.getStringExtra(ARGS_SWITCH_LANGUAGE)
            if (flag == "true") {
                showSwitchLanguageFragment()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setupDataSourceChange()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        mNavController.addOnDestinationChangedListener(navDestListener)

        checkPublicityEntranceEvent()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        mNavController.removeOnDestinationChangedListener(navDestListener)
    }

    override fun onStart() {
        super.onStart()

        if (isFromPublicity) {
            val matchId = intent.getStringExtra(GamePublicityActivity.PUBLICITY_MATCH_ID)
            val gameTypeCode = intent.getStringExtra(GamePublicityActivity.PUBLICITY_GAME_TYPE)
            val gameType = GameType.getGameType(gameTypeCode) ?: GameType.OTHER
            val intentMatchType = intent.getSerializableExtra(GamePublicityActivity.PUBLICITY_MATCH_TYPE)
            val matchType = if (intentMatchType != null) intentMatchType as MatchType else null
            val matchList =
                intent.getParcelableArrayListExtra<MatchInfo>(GamePublicityActivity.PUBLICITY_MATCH_LIST)
            matchId?.let {
                navDetailLiveFragment(
                    matchID = matchId, gameType = gameType, matchType = matchType, matchList = matchList
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        rv_marquee.startAuto()
    }

    override fun onPause() {
        super.onPause()
        rv_marquee.stopAuto()
    }

    override fun initToolBar() {
        iv_logo.setImageResource(R.drawable.ic_logo)
        iv_logo.setOnClickListener {
            viewModel.navMainPage(ThirdGameCategory.MAIN)
            removeBetListFragment()
        }

        iv_language.setImageResource(LanguageManager.getLanguageFlag(this))

        //頭像 當 側邊欄 開/關
        iv_menu.setOnClickListener {
            clickMenuEvent()
        }

        btn_login.setOnClickListener {
            closeLeftMenu()
            startActivity(Intent(this@GameActivity, LoginActivity::class.java))
        }

        btn_register.setOnClickListener {
            closeLeftMenu()
            startActivity(Intent(this@GameActivity, RegisterOkActivity::class.java))
        }

        tv_odds_type.setOnClickListener {
            ChangeOddsTypeDialog().show(supportFragmentManager, null)
        }

        iv_language.setOnClickListener {
            ChangeLanguageDialog(ChangeLanguageDialog.ClearBetListListener {
                viewModel.betInfoRepository.clear()
            }).show(supportFragmentManager, null)
        }
    }

    override fun clickMenuEvent() {
        //若左側側邊欄為開啟狀態, 先將其關閉
        closeLeftMenu()

        if (drawer_layout.isDrawerOpen(nav_right)) drawer_layout.closeDrawers()
        else {
            drawer_layout.openDrawer(nav_right)
            viewModel.getMoney()
        }
    }

    override fun initMenu() {
        try {
            //關閉側邊欄滑動行為
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            //選單選擇結束要收起選單
            val menuFrag =
                supportFragmentManager.findFragmentById(R.id.fragment_menu) as MenuFragment
            menuFrag.setDownMenuListener { drawer_layout.closeDrawers() }
            nav_right.layoutParams.width = MetricsUtil.getMenuWidth() //動態調整側邊欄寬

            //左邊側邊攔v4
            btn_menu_left.visibility = if (isFromPublicity) View.GONE else View.VISIBLE
            nav_left.layoutParams.width = MetricsUtil.getScreenWidth() //動態調整側邊欄寬
            btn_menu_left.setOnClickListener {
                if (!sub_drawer_layout.isDrawerOpen(nav_left)) sub_drawer_layout.openDrawer(nav_left)

                if (drawer_layout.isDrawerOpen(nav_right)) drawer_layout.closeDrawers()
            }
            val leftMenuFragment = supportFragmentManager.findFragmentById(R.id.fl_left_menu) as LeftMenuFragment
            leftMenuFragment.setCloseMenuListener {
                hideSoftKeyboard(this)
                sub_drawer_layout.closeDrawers()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initSubmitBtn() {
        game_submit.setOnClickListener {
            viewModel.submitLeague()
        }
    }

    override fun initBottomNavigation() {
        tv_balance_currency.text = sConfigData?.systemCurrencySign
        tv_balance.text = TextUtil.formatMoney(0.0)
        cl_bet_list_bar.setOnClickListener {
            showBetListPage()
        }
        sport_bottom_navigation.apply {
            setNavigationItemClickListener {
                when (it) {
                    R.id.navigation_home -> {
                        viewModel.navHome()
                        finish()
                        false
                    }
                    R.id.navigation_sport -> {
                        if (tabLayout.selectedTabPosition != getMatchTypeTabPosition(startMatchType)) {
                            getMatchTypeTabPosition(startMatchType)?.let { mainMatchTypePosition ->
                                //賽事類別Tab不在主頁時, 切換至主頁
                                tabLayout.selectTab(tabLayout.getTabAt(mainMatchTypePosition))
                            }
                        } else {
                            if (mNavController.currentDestination?.id != mNavController.graph.startDestination) {
                                //若當前不在起始fragment時, 切換至起始fragment
                                selectTab(getMatchTypeTabPosition(startMatchType))
                            }
                        }
                        true
                    }
                    R.id.navigation_account_history -> {
                        viewModel.navAccountHistory()
                        false
                    }
                    R.id.navigation_transaction_status -> {
                        viewModel.navTranStatus()
                        false
                    }
                    R.id.navigation_my -> {
                        viewModel.navMy()
                        false
                    }
                    else -> false
                }
            }

            setSelected(R.id.navigation_sport)
        }
    }

    override fun showBetListPage() {
        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit,
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit
            )

        betListFragment =
            BetListFragment.newInstance(object : BetListFragment.BetResultListener {
                override fun onBetResult(
                    betResultData: Receipt?,
                    betParlayList: List<ParlayOdd>,
                    isMultiBet: Boolean
                ) {
                    showBetReceiptDialog(betResultData, betParlayList, isMultiBet, R.id.fl_bet_list)
                }
            })

        transaction
            .add(R.id.fl_bet_list, betListFragment, BetListFragment::class.java.simpleName)
            .addToBackStack(BetListFragment::class.java.simpleName)
            .commit()

    }

    override fun getBetListPageVisible(): Boolean {
        return betListFragment.isVisible
    }

    override fun updateBetListCount(num: Int) {
        sport_bottom_navigation.setBetCount(num)
        cl_bet_list_bar.isVisible = num > 0
        line_shadow.isVisible = !cl_bet_list_bar.isVisible
        tv_bet_list_count.text = num.toString()
        if (num > 0) viewModel.getMoney()
    }
    override fun updateBetListOdds(list: MutableList<BetInfoListData>) {
        val multipleOdds = getMultipleOdds(list)
        cl_bet_list_bar.tvOdds.text = multipleOdds
    }

    override fun showLoginNotify() {
        snackBarLoginNotify.apply {
            setAnchorView(R.id.game_bottom_navigation)
            show()
        }
    }

    override fun showMyFavoriteNotify(myFavoriteNotifyType: Int) {
        setSnackBarMyFavoriteNotify(myFavoriteNotifyType)
        snackBarMyFavoriteNotify?.apply {
            setAnchorView(R.id.game_bottom_navigation)
            show()
        }
    }

    //公告
    private fun initRvMarquee() {
        game_message.setOnClickListener {
            closeLeftMenu()
            startActivity(Intent(this, NewsActivity::class.java))
        }
        game_message.visibility = if (isFromPublicity) View.GONE else View.VISIBLE
        rv_marquee.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_marquee.adapter = mMarqueeAdapter
    }

    private fun initTabLayout() {
        tabLayout.visibility = if (isFromPublicity) View.GONE else View.VISIBLE

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                dismissSwitchLanguageFragment()
                selectTab(tab?.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        OverScrollDecoratorHelper.setUpOverScroll(tabLayout)
    }

    private fun refreshTabLayout(sportMenuResult: SportMenuResult?) {
        try {
            val countInPlay =
                sportMenuResult?.sportMenuData?.menu?.inPlay?.items?.sumBy { it.num } ?: 0
            val countAtStart =
                sportMenuResult?.sportMenuData?.atStart?.items?.sumBy { it.num } ?: 0
            val countToday =
                sportMenuResult?.sportMenuData?.menu?.today?.items?.sumBy { it.num } ?: 0
            val countEarly =
                sportMenuResult?.sportMenuData?.menu?.early?.items?.sumBy { it.num } ?: 0
            val countParlay =
                sportMenuResult?.sportMenuData?.menu?.parlay?.items?.sumBy { it.num } ?: 0
            val countOutright =
                sportMenuResult?.sportMenuData?.menu?.outright?.items?.sumBy { it.num } ?: 0
            val countEps =
                sportMenuResult?.sportMenuData?.menu?.eps?.items?.sumBy { it.num } ?: 0

            //20220728 不要有主頁
            /*tabLayout.getTabAt(getMatchTypeTabPosition(MatchType.MAIN) ?: 0)?.view?.visibility = View.GONE
            tabLayout.getTabAt(getMatchTypeTabPosition(MatchType.MAIN) ?: 0)?.customView?.apply {
                visibility = View.GONE
                *//*tv_title?.setTextWithStrokeWidth(getString(R.string.home_tan_main), 0.7f)
                tv_number?.text = countParlay.plus(countInPlay).plus(countAtStart).plus(countToday).plus(countEarly)
                    .plus(countOutright).plus(countEps).toString()*//*
            }*/

            tabLayout.getTabAt(getMatchTypeTabPosition(MatchType.IN_PLAY) ?: 1)?.customView?.apply {
                tv_title?.setTextWithStrokeWidth(getString(R.string.home_tab_in_play), 0.7f)
                tv_number?.text = countInPlay.toString()
            }

            tabLayout.getTabAt(getMatchTypeTabPosition(MatchType.AT_START) ?: 2)?.customView?.apply {
                tv_title?.setTextWithStrokeWidth(getString(R.string.home_tab_at_start), 0.7f)
                tv_number?.text = countAtStart.toString()
            }

            tabLayout.getTabAt(getMatchTypeTabPosition(MatchType.TODAY) ?: 3)?.customView?.apply {
                tv_title?.setTextWithStrokeWidth(getString(R.string.home_tab_today), 0.7f)
                tv_number?.text = countToday.toString()
            }
            tabLayout.getTabAt(getMatchTypeTabPosition(MatchType.PARLAY) ?: 4)?.customView?.apply {
                tv_title?.setTextWithStrokeWidth(getString(R.string.home_tab_parlay), 0.7f)
                tv_number?.text = countParlay.toString()
            }

            tabLayout.getTabAt(getMatchTypeTabPosition(MatchType.EARLY) ?: 5)?.customView?.apply {
                tv_title?.setTextWithStrokeWidth(getString(R.string.home_tab_early), 0.7f)
                tv_number?.text = countEarly.toString()
            }

            tabLayout.getTabAt(getMatchTypeTabPosition(MatchType.OUTRIGHT) ?: 6)?.customView?.apply {
                tv_title?.setTextWithStrokeWidth(getString(R.string.home_tab_outright), 0.7f)
                tv_number?.text = countOutright.toString()
            }



            //0401需求先隱藏特優賠率
//            val tabEps = tabLayout.getTabAt(7)?.customView
//            tabEps?.tv_title?.setText(R.string.home_tab_eps)
//            tabEps?.tv_number?.text = countEps.toString()

            //英文 越南文稍微加寬padding 不然會太擠
            if (LanguageManager.getSelectLanguage(this) != LanguageManager.Language.ZH) {
                for (i in 0 until tabLayout.tabCount) {
                    tabLayout.getTabAt(i)?.customView.apply {
                        this?.setPadding(8.dp, 0, 16.dp, 0)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val matchTypeTabPositionMap = mapOf<MatchType, Int>(
        //20220728 隱藏主頁
        /*MatchType.MAIN to 0,
        MatchType.IN_PLAY to 1,
        MatchType.AT_START to 2,
        MatchType.TODAY to 3,
        MatchType.EARLY to 4,
        MatchType.OUTRIGHT to 5,
        MatchType.PARLAY to 6,
        MatchType.EPS to 7*/
        MatchType.IN_PLAY to 0,
        MatchType.AT_START to 1,
        MatchType.TODAY to 2,
        MatchType.EARLY to 3,
        MatchType.PARLAY to 4,
        MatchType.OUTRIGHT to 5,
        MatchType.EPS to 6,
        MatchType.MAIN to 99
    )

    /**
     * 根據MatchTypeTabPositionMap獲取MatchType的tab position
     *
     * @see MatchTypeTabPositionMap
     */
    private fun getMatchTypeTabPosition(matchType: MatchType?): Int? = when (matchType) {
        null -> {
            Timber.e("Unable to get $matchType tab position")
            null
        }
        else -> {
            when (val tabPosition = matchTypeTabPositionMap[matchType]) {
                null -> {
                    Timber.e("There is not tab position of $matchType")
                    null
                }
                else -> {
                    tabPosition
                }
            }
        }
    }

    private fun selectTab(position: Int?) {
        if (position == null) return

        when (position) {
            getMatchTypeTabPosition(MatchType.MAIN) -> {
                viewModel.switchMatchType(MatchType.MAIN)
            }
            getMatchTypeTabPosition(MatchType.IN_PLAY) -> {
                viewModel.switchMatchType(MatchType.IN_PLAY)
            }
            getMatchTypeTabPosition(MatchType.AT_START) -> {
                viewModel.switchMatchType(MatchType.AT_START)
            }
            getMatchTypeTabPosition(MatchType.TODAY) -> {
                viewModel.switchMatchType(MatchType.TODAY)
            }
            getMatchTypeTabPosition(MatchType.EARLY) -> {
                viewModel.switchMatchType(MatchType.EARLY)
            }
            getMatchTypeTabPosition(MatchType.OUTRIGHT) -> {
                /**
                 * 若mOutrightLeagueId有值的話, 此行為為主頁點擊聯賽跳轉至冠軍頁, 跳轉行為於HomeFragment處理
                 *
                 * @see org.cxct.sportlottery.ui.game.home.HomeFragment.navGameOutright
                 */
                if (mOutrightLeagueId.isNullOrEmpty()) {
                    viewModel.switchMatchType(MatchType.OUTRIGHT)
                }
            }
            getMatchTypeTabPosition(MatchType.PARLAY) -> {
                viewModel.switchMatchType(MatchType.PARLAY)
            }
            getMatchTypeTabPosition(MatchType.EPS) -> {
                viewModel.switchMatchType(MatchType.EPS)
            }
        }
    }

    private fun navDetailLiveFragment(
        matchID: String,
        gameType: GameType,
        matchType: MatchType? = null,
        matchList: ArrayList<MatchInfo>? = null
    ) {
        val detailMatchType = matchType ?: MatchType.DETAIL
        when (mNavController.currentDestination?.id) {
            R.id.homeFragment -> {
                val action = HomeFragmentDirections.actionHomeFragmentToOddsDetailLiveFragment(
                    detailMatchType,
                    gameType,
                    matchID
                )
                mNavController.navigate(action)
            }
            R.id.gameV3Fragment -> {
                val action = GameV3FragmentDirections.actionGameV3FragmentToOddsDetailLiveFragment(
                    detailMatchType,
                    gameType,
                    matchID
                )
                mNavController.navigate(action)
            }
            R.id.gameLeagueFragment -> {
                val action = HomeFragmentDirections.actionHomeFragmentToOddsDetailLiveFragment(
                    detailMatchType,
                    gameType,
                    matchID
                )
                mNavController.navigate(action)
            }
        }
    }

    private fun navGameFragment(matchType: MatchType) {
        //TODO 確認有什麼情況下會是MatchType.MAIN的，若沒有的話可以濾掉
        when (mNavController.currentDestination?.id) {
            R.id.homeFragment -> {
                val action = HomeFragmentDirections.actionHomeFragmentToGameFragment(matchType)
                mNavController.navigate(action)
            }
            R.id.gameV3Fragment -> {
                val action = GameV3FragmentDirections.actionGameFragmentToGameFragment(matchType)
                val navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
                mNavController.navigate(action, navOptions)
            }
            R.id.leagueFilterFragment -> {
                val action =
                    LeagueFilterFragmentDirections.actionLeagueFilterFragmentToGameV3Fragment(
                        matchType
                    )
                mNavController.navigate(action)
            }
            R.id.gameLeagueFragment -> {
                val action =
                    GameLeagueFragmentDirections.actionGameLeagueFragmentToGameV3Fragment(matchType)
                mNavController.navigate(action)
            }
            R.id.gameOutrightMoreFragment -> {
                val action =
                    GameOutrightMoreFragmentDirections.actionGameOutrightMoreFragmentToGameV3Fragment(
                        matchType
                    )
                mNavController.navigate(action)
            }
            R.id.oddsDetailLiveFragment -> {
                val action =
                    OddsDetailLiveFragmentDirections.actionOddsDetailLiveFragmentToGameV3Fragment(
                        matchType
                    )
                mNavController.navigate(action)
            }
        }
    }

    private fun navHomeFragment() {
        //TODO 此處有個隱藏的Bug, 在進到這個fun前, currentDestination都已經因為觀察到curMatchType的變化進入navGameFragment()而移動至gameV3Fragment
        when (mNavController.currentDestination?.id) {

            R.id.homeFragment -> {
            }
            R.id.gameV3Fragment -> {
                val action = GameV3FragmentDirections.actionGameV3FragmentToHomeFragment()
                val navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
                mNavController.navigate(action, navOptions)
            }
            R.id.leagueFilterFragment -> {
                val action =
                    LeagueFilterFragmentDirections.actionLeagueFilterFragmentToHomeFragment()
                mNavController.navigate(action)
            }

            R.id.gameLeagueFragment -> {
                val action =
                    GameLeagueFragmentDirections.actionGameLeagueFragmentToHomeFragment()
                mNavController.navigate(action)
            }

            R.id.gameOutrightMoreFragment -> {
                val action =
                    GameOutrightMoreFragmentDirections.actionGameOutrightMoreFragmentToHomeFragment()
                mNavController.navigate(action)
            }
            R.id.oddsDetailLiveFragment -> {
                val action =
                    OddsDetailLiveFragmentDirections.actionOddsDetailLiveFragmentToHomeFragment()
                mNavController.navigate(action)
            }
        }
    }

    override fun onBackPressed() {
        //返回鍵優先關閉投注單fragment
        if (supportFragmentManager.backStackEntryCount != 0) {
            for (i in 0 until supportFragmentManager.backStackEntryCount) {
                supportFragmentManager.popBackStack()
            }
            return
        }
        //關閉drawer
        if (drawer_layout.isDrawerOpen(nav_right)) {
            drawer_layout.closeDrawers()
            return
        }
        if (sub_drawer_layout.isDrawerOpen(nav_left)) {
            sub_drawer_layout.closeDrawers()
            return
        }
        when (mNavController.currentDestination?.id) {
            R.id.gameV3Fragment -> {
                //特殊賽事返回時，不回到主頁
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.game_container) as NavHostFragment
                val gameV3Fragment =
                    navHostFragment.childFragmentManager.fragments.firstOrNull() as GameV3Fragment
                when (gameV3Fragment.arguments?.getSerializable("matchType") as MatchType) {
                    MatchType.OTHER -> {
                        goTab(tabLayout.selectedTabPosition)
                    }
                    MatchType.IN_PLAY -> {
                        //當前為滾球時，點back返回宣傳頁
                        GamePublicityActivity.reStart(this)
                    }
                    else -> {
                        matchTypeTabPositionMap[MatchType.IN_PLAY]?.let {
                            goTab(it)
                        }
                    }
                }
            }
            R.id.homeFragment -> {
                //首頁時，點back返回宣傳頁
                GamePublicityActivity.reStart(this)
            }
            else -> mNavController.navigateUp()
        }
    }

    //用戶登入公告訊息彈窗
    private var mNewsDialog: NewsDialog? = null
    private fun setNewsDialog(messageListResult: MessageListResult) {

        //未登入、遊客登入都要顯示彈窗
        //顯示規則：帳號登入前= 公告含登入前、帳號登入後= 公告含登入前+登入後
        var list = listOf<Row>()
        list = if (viewModel.isLogin.value == true)
            messageListResult.rows?.filter { it.type.toInt() != 1 } ?: listOf()
        else
            messageListResult.rows?.filter { it.type.toInt() == 3 } ?: listOf()

        if (!list.isNullOrEmpty()) {
            if (!MultiLanguagesApplication.getInstance()?.isNewsShow()!!) {
                mNewsDialog?.dismiss()
                mNewsDialog = NewsDialog(list)
                mNewsDialog?.show(supportFragmentManager, null)
                MultiLanguagesApplication.getInstance()?.setIsNewsShow(true)
            }
        }
    }

    private fun initObserve() {
        viewModel.userMoney.observe(this) {
            it?.let { money ->
                tv_balance.text = TextUtil.formatMoney(money)
            }
        }
        viewModel.settlementNotificationMsg.observe(this) {
            val message = it.getContentIfNotHandled()
            message?.let { messageNotnull -> view_notification.addNotification(messageNotnull) }
        }

        viewModel.isLogin.observe(this) {
            getAnnouncement()
        }

        //使用者沒有電話號碼
        viewModel.showPhoneNumberMessageDialog.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (!b) phoneNumCheckDialog(this, supportFragmentManager)
            }
        }

        viewModel.showBetUpperLimit.observe(this) {
            if (it.getContentIfNotHandled() == true)
                snackBarBetUpperLimitNotify.apply {
                    setAnchorView(R.id.game_bottom_navigation)
                    show()
                }
        }

        viewModel.messageListResult.observe(this) {
            it.getContentIfNotHandled()?.let { result ->
                updateUiWithResult(result)
            }
        }

        viewModel.nowTransNum.observe(this) {
            navigation_transaction_status.trans_number.text = it.toString()
        }

        viewModel.specialEntrance.observe(this) {
            hideLoading()
            if (it?.couponCode.isNullOrEmpty()) {
                when (it?.entranceMatchType) {
                    MatchType.MAIN -> {
                        //do nothing
                    }
                    MatchType.OTHER -> {
                        goTab(3)
                    }
                    MatchType.DETAIL -> {
                        it.matchID?.let { matchId ->
                            navDetailLiveFragment(matchId, it.gameType ?: GameType.OTHER, it.gameMatchType)
                        }
                    }
                    else -> {
                        getMatchTypeTabPosition(it?.entranceMatchType)?.let { matchTypePosition ->
                            goTab(matchTypePosition)
                        }
                    }
                }
            } else if (it?.entranceMatchType == MatchType.DETAIL) {

            } else {
                navGameFragment(it!!.entranceMatchType)
            }
        }

        //distinctUntilChanged() -> 相同的matchType僅會執行一次，有變化才會observe
        viewModel.curMatchType.distinctUntilChanged().observe(this) {
            it?.let {
                val tabSelectedPosition = tabLayout.selectedTabPosition
                when (it) {
                    MatchType.MAIN -> {
                        if (tabSelectedPosition == getMatchTypeTabPosition(MatchType.MAIN))
                            navHomeFragment()
                    }
                    else -> {
                        //僅有要切換的MatchType與當前選中的Tab相同時才繼續進行後續的切頁行為, 避免快速切頁導致切頁邏輯進入無窮迴圈
                        when {
                            it == MatchType.IN_PLAY && tabSelectedPosition == getMatchTypeTabPosition(MatchType.IN_PLAY) ||
                                    it == MatchType.AT_START && tabSelectedPosition == getMatchTypeTabPosition(MatchType.AT_START) ||
                                    it == MatchType.TODAY && tabSelectedPosition == getMatchTypeTabPosition(MatchType.TODAY) ||
                                    it == MatchType.EARLY && tabSelectedPosition == getMatchTypeTabPosition(MatchType.EARLY) ||
                                    it == MatchType.OUTRIGHT && tabSelectedPosition == getMatchTypeTabPosition(MatchType.OUTRIGHT) ||
                                    it == MatchType.PARLAY && tabSelectedPosition == getMatchTypeTabPosition(MatchType.PARLAY)
                            -> {
                                navGameFragment(it)
                            }
                            else -> {
                                //do nothing
                            }
                        }

                    }
                }
            }
        }

        viewModel.sportMenuResult.observe(this) {
            hideLoading()
            updateUiWithResult(it)
        }

        viewModel.userInfo.observe(this) {
            updateAvatar(it?.iconUrl)
        }

        viewModel.errorPromptMessage.observe(this) {
            it.getContentIfNotHandled()
                ?.let { message -> showErrorPromptDialog(getString(R.string.prompt), message) {} }

        }

        viewModel.leagueSelectedList.observe(this) {
            game_submit.apply {
                visibility = if (it.isEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

                text = getString(R.string.button_league_submit, it.size)
            }
        }

        viewModel.showBetInfoSingle.observe(this) {
            it?.getContentIfNotHandled()?.let {
                showBetListPage()
            }
        }

        viewModel.navPublicityPage.observe(this) {
            GamePublicityActivity.reStart(this)
        }
    }

    /**
     * 前往指定的賽事種類
     * @since 若已經在該賽事種類, 點擊Tab不會觸發OnTabSelectedListener
     */
    private fun goTab(tabPosition: Int) {
        if (tabLayout.selectedTabPosition != tabPosition) {
            //賽事類別Tab不在滾球時, 點擊滾球Tab
            tabLayout.getTabAt(tabPosition)?.select()
        } else {
            selectTab(tabPosition)
        }
    }

    fun setupBetData(fastBetDataBean: FastBetDataBean) {
        viewModel.updateMatchBetListData(fastBetDataBean)
    }

    fun showSwitchLanguageFragment() {
        startActivity(Intent(this@GameActivity, SwitchLanguageActivity::class.java))
    }

    fun dismissSwitchLanguageFragment() {
        if (isSwitchLanguageFragmentVisible()) {
            supportFragmentManager.popBackStack()
        }
    }

    private fun isSwitchLanguageFragmentVisible(): Boolean {
        val fragments: List<Fragment> = supportFragmentManager.fragments
        for (fragment in fragments) {
            if (fragment is SwitchLanguageFragment) {
                if (fragment.isVisible) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 初始化客服按鈕
     * 另外透過DestinationChangedListener控制客服按鈕出現或隱藏
     * @see navDestListener
     * @see updateServiceButtonVisibility
     */
    private fun initServiceButton() {
        //2022/4/21需求：客服只在首頁和宣傳頁、維護頁出現
        btn_floating_service.setView(this)
    }

    override fun updateUiWithLogin(isLogin: Boolean) {
        if (isLogin) {
            btn_login.visibility = View.GONE
            iv_menu.visibility = View.VISIBLE
            iv_notice.visibility = View.VISIBLE
            btn_register.visibility = View.GONE
            toolbar_divider.visibility = View.GONE
            iv_head.visibility = View.GONE
            tv_odds_type.visibility = View.GONE
        } else {
            btn_login.visibility = View.VISIBLE
            btn_register.visibility = View.VISIBLE
            toolbar_divider.visibility = View.VISIBLE
            iv_head.visibility = View.GONE
            tv_odds_type.visibility = View.GONE
            iv_menu.visibility = View.GONE
            iv_notice.visibility = View.GONE
            btn_register.setVisibilityByCreditSystem()
            toolbar_divider.setVisibilityByCreditSystem()
        }
    }

    override fun updateOddsType(oddsType: OddsType) {
        tv_odds_type.text = getString(oddsType.res)
    }

    override fun navOneSportPage(thirdGameCategory: ThirdGameCategory?) {
        if (thirdGameCategory != null) {
            val intent = Intent(this, MainActivity::class.java)
                .putExtra(ARGS_THIRD_GAME_CATE, thirdGameCategory)
            startActivity(intent)

            return
        }

        GamePublicityActivity.reStart(this)
    }

    private fun updateUiWithResult(messageListResult: MessageListResult?) {
        val titleList: MutableList<String> = mutableListOf()
        messageListResult?.let {
            it.rows?.forEach { data ->
                if (data.type.toInt() == 1) titleList.add(data.title + " - " + data.message)
            }

            mMarqueeAdapter.setData(titleList)

            if (messageListResult.success && titleList.size > 0) {
                rv_marquee.startAuto(false) //啟動跑馬燈
            } else {
                rv_marquee.stopAuto(true) //停止跑馬燈
            }
        }
    }

    private fun updateUiWithResult(sportMenuResult: SportMenuResult?) {
        if (sportMenuResult?.success == true) {
            refreshTabLayout(sportMenuResult)
        }
    }

    private fun updateAvatar(iconUrl: String?) {
        Glide.with(this).load(iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.img_avatar_default)).into(
                iv_head
            ) //載入頭像
    }

    private fun getAnnouncement() {
        viewModel.getAnnouncement()
    }

    private fun updateSelectTabState(matchType: MatchType?) {
        matchTypeTabPositionMap[matchType]?.let {
            updateSelectTabState(it)
        }
    }

    private fun updateSelectTabState(position: Int) {
        val tab = tabLayout.getTabAt(position)?.customView

        tab?.let {
            clearSelectTabState()
            tabLayout.getTabAt(position)?.select()
            it.tv_title?.isSelected = true
            it.tv_number?.isSelected = true
        }
    }

    private fun clearSelectTabState() {
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)?.customView

            tab?.tv_title?.isSelected = false
            tab?.tv_number?.isSelected = false
        }
    }

    private fun removeBetListFragment() {
        supportFragmentManager.beginTransaction().remove(betListFragment).commit()
    }

    private fun closeLeftMenu() {
        val leftMenuFragment = supportFragmentManager.findFragmentById(R.id.fl_left_menu) as LeftMenuFragment
        leftMenuFragment.clearLeftMenu()
        if (sub_drawer_layout.isDrawerOpen(nav_left)) sub_drawer_layout.closeDrawers()
    }

    override fun onCloseMenu() {
        super.onCloseMenu()

        closeLeftMenu()
    }

    override fun onDestroy() {
        expandCheckList.clear()
        HomePageStatusManager.clear()
        mNavController.removeOnDestinationChangedListener(navDestListener)
        super.onDestroy()
    }

    private fun setupDataSourceChange() {
        setDataSourceChangeEvent {
            viewModel.fetchDataFromDataSourceChange(
                matchTypeTabPositionMap.filterValues { it == tabLayout.selectedTabPosition }.entries.first().key
            )
        }
    }

    /**
     * 檢查是否有從宣傳頁入口跳轉的事件
     *
     * @see org.cxct.sportlottery.ui.game.publicity.PublicityNewFragment.jumpToTheSport
     */
    private fun checkPublicityEntranceEvent() {
        val publicitySportEntrance =
            intent.getSerializableExtra(ARGS_PUBLICITY_SPORT_ENTRANCE) as? PublicitySportEntrance
        publicitySportEntrance?.let {
            viewModel.navSpecialEntrance(it.matchType, it.gameType)
        }
    }

}