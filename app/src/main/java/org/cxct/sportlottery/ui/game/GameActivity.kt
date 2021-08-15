package org.cxct.sportlottery.ui.game

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.home_cate_tab.view.*
import kotlinx.android.synthetic.main.sport_bottom_navigation.view.*
import kotlinx.android.synthetic.main.view_bottom_navigation_sport.*
import kotlinx.android.synthetic.main.view_game_tab_match_type_v4.*
import kotlinx.android.synthetic.main.view_message.*
import kotlinx.android.synthetic.main.view_nav_left.*
import kotlinx.android.synthetic.main.view_nav_right.*
import kotlinx.android.synthetic.main.view_toolbar_main.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.ui.MarqueeAdapter
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.bet.list.BetInfoCarDialog
import org.cxct.sportlottery.ui.game.betList.BetListFragment
import org.cxct.sportlottery.ui.game.betList.receipt.BetReceiptFragment
import org.cxct.sportlottery.ui.game.data.SpecialEntranceSource
import org.cxct.sportlottery.ui.game.hall.GameV3FragmentDirections
import org.cxct.sportlottery.ui.game.home.HomeFragmentDirections
import org.cxct.sportlottery.ui.game.league.GameLeagueFragmentDirections
import org.cxct.sportlottery.ui.game.menu.LeftMenuFragment
import org.cxct.sportlottery.ui.game.outright.GameOutrightFragmentDirections
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.MainActivity.Companion.ARGS_THIRD_GAME_CATE
import org.cxct.sportlottery.ui.main.accountHistory.AccountHistoryActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.menu.ChangeOddsTypeDialog
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.ui.menu.MenuLeftFragment
import org.cxct.sportlottery.ui.odds.OddsDetailFragmentDirections
import org.cxct.sportlottery.ui.odds.OddsDetailLiveFragmentDirections
import org.cxct.sportlottery.ui.transactionStatus.TransactionStatusActivity
import org.cxct.sportlottery.util.MetricsUtil


class GameActivity : BaseSocketActivity<GameViewModel>(GameViewModel::class) {

    private val mMarqueeAdapter by lazy { MarqueeAdapter() }
    private val mNavController by lazy { findNavController(R.id.game_container) }
    private val navDestListener by lazy {
        NavController.OnDestinationChangedListener { _, destination, arguments ->
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

                R.id.gameOutrightFragment -> {
                    updateSelectTabState(MatchType.OUTRIGHT)
                }

                R.id.oddsDetailFragment -> {
                    updateSelectTabState(arguments?.get("matchType") as MatchType)
                }

                R.id.oddsDetailLiveFragment -> {
                    updateSelectTabState(MatchType.IN_PLAY)
                }
            }
        }
    }

    private val mMenuLeftListener = object : MenuLeftFragment.MenuLeftListener {
        override fun onClick(id: Int) {
            when (id) {
                R.id.btn_lobby -> iv_logo.performClick()
                R.id.menu_sport_game -> tabLayout.getTabAt(0)?.select()
                R.id.menu_in_play -> tabLayout.getTabAt(1)?.select()
                R.id.menu_date_row_today -> tabLayout.getTabAt(3)?.select()
                R.id.menu_early -> tabLayout.getTabAt(4)?.select()
                R.id.menu_parlay -> tabLayout.getTabAt(5)?.select()
                R.id.menu_champion -> tabLayout.getTabAt(6)?.select()
                R.id.menu_soccer -> goToSportGame(GameType.FT)
                R.id.menu_basketball -> goToSportGame(GameType.BK)
                R.id.menu_tennis -> goToSportGame(GameType.TN)
                R.id.menu_volleyball -> goToSportGame(GameType.VB)
                R.id.menu_cg_lottery -> goToMainActivity(ThirdGameCategory.CGCP)
                R.id.menu_live_game -> goToMainActivity(ThirdGameCategory.LIVE)
                R.id.menu_poker_game -> goToMainActivity(ThirdGameCategory.QP)
                R.id.menu_slot_game -> goToMainActivity(ThirdGameCategory.DZ)
                R.id.menu_fish_game -> goToMainActivity(ThirdGameCategory.BY)
            }
        }
    }

    enum class Page { ODDS_DETAIL, OUTRIGHT }

    var canOpenBetInfoPage: Boolean = false //判斷是否能開啟投注單頁面

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        setupNoticeButton(btn_notice)
        initToolBar()
        initMenu()
        initSubmitBtn()
        initBottomNavigation()
        initRvMarquee()
        initTabLayout()
        initObserve()

        queryData()
    }

    override fun onResume() {
        super.onResume()
        rv_marquee.startAuto()

        mNavController.addOnDestinationChangedListener(navDestListener)

    }

    override fun onPause() {
        super.onPause()
        rv_marquee.stopAuto()

        mNavController.removeOnDestinationChangedListener(navDestListener)
    }

    private fun goToSportGame(gameType: GameType) {
        //規則：
        //1. 優先跳轉到當前頁籤下選擇要跳轉的球類賽事
        //2. 若此當前頁籤無該種球類比賽，則後續導入優先順序為 今日 > 早盤 > 串關
        //3. 若扔沒有則顯示無賽事的圖片
        //4. 若扔沒有則顯示無賽事的圖片
        val matchType = when (tabLayout.selectedTabPosition) {
            0, 3 -> MatchType.TODAY
            1 -> MatchType.IN_PLAY
            4 -> MatchType.EARLY
            5 -> MatchType.PARLAY
            6 -> MatchType.OUTRIGHT
            else -> MatchType.AT_START
        }

        viewModel.navSpecialEntrance(SpecialEntranceSource.LEFT_MENU, matchType, gameType)
    }

    private fun goToMainActivity(thirdGameCategory: ThirdGameCategory) {
        val intent = Intent(this, MainActivity::class.java)
            .putExtra(ARGS_THIRD_GAME_CATE, thirdGameCategory)
        startActivity(intent)
    }

    private fun initToolBar() {
        iv_logo.setImageResource(R.drawable.ic_logo)
        iv_logo.setOnClickListener {
            goToMainActivity(ThirdGameCategory.MAIN)
        }

        //頭像 當 側邊欄 開/關
        iv_head.setOnClickListener {
            if (drawer_layout.isDrawerOpen(nav_right)) drawer_layout.closeDrawers()
            else {
                drawer_layout.openDrawer(nav_right)
            }
        }

        btn_login.setOnClickListener {
            startActivity(Intent(this@GameActivity, LoginActivity::class.java))
        }

        btn_register.setOnClickListener {
            startActivity(Intent(this@GameActivity, RegisterActivity::class.java))
        }

        tv_odds_type.setOnClickListener {
            ChangeOddsTypeDialog().show(supportFragmentManager, null)
        }
    }

    private fun initMenu() {
        try {
            //關閉側邊欄滑動行為
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            //選單選擇結束要收起選單
            val menuFrag =
                supportFragmentManager.findFragmentById(R.id.fragment_menu) as MenuFragment
            menuFrag.setDownMenuListener { drawer_layout.closeDrawers() }
            nav_right.layoutParams.width = MetricsUtil.getMenuWidth() //動態調整側邊欄寬

            //左邊側邊攔v4
            btn_menu_left.setOnClickListener {
                LeftMenuFragment().show(supportFragmentManager, LeftMenuFragment::class.java.simpleName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface OnMenuClickListener {
        fun onClick(menuStatus: Int)
    }

    private fun initSubmitBtn() {
        game_submit.setOnClickListener {
            viewModel.submitLeague()
        }
    }

    private fun initBottomNavigation() {
        initNavigationListener()
    }

    private fun initNavigationListener() {
        sport_bottom_navigation.setNavigationItemClickListener {
            when (it) {
                R.id.navigation_sport -> {
                    //TODO navigate sport home
                    true
                }
                R.id.navigation_game -> {
                    //TODO navigate sport game
                    true
                }
                R.id.item_bet_list -> {
                    //TODO 邏輯移動 see: BetInfoListDialog
//                    showBetListDialog()
                    showBetListPage()
                    false
                }
                R.id.navigation_account_history -> {
                    startActivity(Intent(this@GameActivity, AccountHistoryActivity::class.java))
                    false
                }
                R.id.navigation_transaction_status -> {
                    startActivity(Intent(this@GameActivity, TransactionStatusActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun showBetListPage() {
        if (!canOpenBetInfoPage) return

        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit,
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit
            )
        val betListFragment = BetListFragment.newInstance(object : BetListFragment.BetResultListener {
            override fun onBetResult(betResultData: Receipt?, betParlayList: List<ParlayOdd>) {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.push_right_to_left_enter,
                        R.anim.pop_bottom_to_top_exit,
                        R.anim.push_right_to_left_enter,
                        R.anim.pop_bottom_to_top_exit
                    )
                    .replace(R.id.fl_bet_list, BetReceiptFragment.newInstance(betResultData, betParlayList))
                    .addToBackStack(BetReceiptFragment::class.java.simpleName)
                    .commit()
            }

        })
        transaction
            .add(R.id.fl_bet_list, betListFragment)
            .addToBackStack(BetListFragment::class.java.simpleName)
            .commit()
    }

    //公告
    private fun initRvMarquee() {
        rv_marquee.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_marquee.adapter = mMarqueeAdapter
    }

    private fun initTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectTab(tab?.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                selectTab(tab?.position)
            }
        })
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

            val tabAll = tabLayout.getTabAt(0)?.customView
            tabAll?.tv_title?.setText(R.string.home_tan_main)
            tabAll?.tv_number?.text = countParlay.toString() //等於串關數量

            val tabInPlay = tabLayout.getTabAt(1)?.customView
            tabInPlay?.tv_title?.setText(R.string.home_tab_in_play)
            tabInPlay?.tv_number?.text = countInPlay.toString()

            val tabAtStart = tabLayout.getTabAt(2)?.customView
            tabAtStart?.tv_title?.setText(R.string.home_tab_at_start)
            tabAtStart?.tv_number?.text = countAtStart.toString()

            val tabToday = tabLayout.getTabAt(3)?.customView
            tabToday?.tv_title?.setText(R.string.home_tab_today)
            tabToday?.tv_number?.text = countToday.toString()

            val tabEarly = tabLayout.getTabAt(4)?.customView
            tabEarly?.tv_title?.setText(R.string.home_tab_early)
            tabEarly?.tv_number?.text = countEarly.toString()

            val tabParlay = tabLayout.getTabAt(5)?.customView
            tabParlay?.tv_title?.setText(R.string.home_tab_parlay)
            tabParlay?.tv_number?.text = countParlay.toString()

            val tabOutright = tabLayout.getTabAt(6)?.customView
            tabOutright?.tv_title?.setText(R.string.home_tab_outright)
            tabOutright?.tv_number?.text = countOutright.toString()

            val tabEps = tabLayout.getTabAt(7)?.customView
            tabEps?.tv_title?.setText(R.string.home_tab_eps)
            tabEps?.tv_number?.text = countEps.toString()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun selectTab(position: Int?) {
        when (position) {
            0 -> {
                mNavController.popBackStack(R.id.homeFragment, false)
            }
            1 -> {
                viewModel.switchMatchType(MatchType.IN_PLAY)
                loading()
            }
            2 -> {
                viewModel.switchMatchType(MatchType.AT_START)
                loading()
            }
            3 -> {
                viewModel.switchMatchType(MatchType.TODAY)
                loading()
            }
            4 -> {
                viewModel.switchMatchType(MatchType.EARLY)
                loading()
            }
            5 -> {
                viewModel.switchMatchType(MatchType.PARLAY)
                loading()
            }
            6 -> {
                viewModel.switchMatchType(MatchType.OUTRIGHT)
                loading()
            }
            7 -> {
                viewModel.switchMatchType(MatchType.EPS)
                loading()
            }
        }
    }

    private fun navGameFragment(matchType: MatchType) {
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
            R.id.gameLeagueFragment -> {
                val action =
                    GameLeagueFragmentDirections.actionGameLeagueFragmentToGameV3Fragment(matchType)
                mNavController.navigate(action)
            }
            R.id.gameOutrightFragment -> {
                val action =
                    GameOutrightFragmentDirections.actionGameOutrightFragmentToGameV3Fragment(
                        matchType
                    )
                mNavController.navigate(action)
            }
            R.id.oddsDetailFragment -> {
                val action =
                    OddsDetailFragmentDirections.actionOddsDetailFragmentToGameV3Fragment(matchType)
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

    override fun onBackPressed() {
        //返回鍵優先關閉投注單fragment
        if (supportFragmentManager.backStackEntryCount != 0) {
            for (i in 0 until supportFragmentManager.backStackEntryCount) {
                supportFragmentManager.popBackStack()
            }
            return
        }
        when (mNavController.currentDestination?.id) {
            R.id.gameLeagueFragment, R.id.gameOutrightFragment, R.id.gameOutrightMoreFragment, R.id.oddsDetailFragment, R.id.oddsDetailLiveFragment -> {
                mNavController.navigateUp()
            }

            R.id.gameV3Fragment -> {
                tabLayout.getTabAt(0)?.select()
            }

            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun initObserve() {
        viewModel.settlementNotificationMsg.observe(this, {
            val message = it.getContentIfNotHandled()
            message?.let { messageNotnull -> view_notification.addNotification(messageNotnull) }
        })

        viewModel.isLogin.observe(this, {
            updateUiWithLogin(it)
            getAnnouncement()
        })

        viewModel.messageListResult.observe(this, {
            updateUiWithResult(it)
        })

        viewModel.specialEntrance.observe(this, {
            it?.let {
                app_bar_layout.setExpanded(true, false)

                when (it.matchType) {
                    MatchType.IN_PLAY -> {
                        tabLayout.getTabAt(1)?.select()
                    }
                    MatchType.AT_START -> {
                        tabLayout.getTabAt(2)?.select()
                    }
                    MatchType.TODAY -> {
                        tabLayout.getTabAt(3)?.select()
                    }
                    MatchType.EARLY -> {
                        tabLayout.getTabAt(4)?.select()
                    }
                    MatchType.PARLAY -> {
                        tabLayout.getTabAt(5)?.select()
                    }
                    MatchType.OUTRIGHT -> {
                        tabLayout.getTabAt(6)?.select()
                    }
                    MatchType.EPS -> {
                        tabLayout.getTabAt(7)?.select()
                    }
                }
            }
        })

        viewModel.curMatchType.observe(this, {
            it?.let {
                navGameFragment(it)
            }
        })

        viewModel.sportMenuResult.observe(this, {
            hideLoading()
            updateUiWithResult(it)
        })

        viewModel.userInfo.observe(this, {
            updateAvatar(it?.iconUrl)
        })

        viewModel.oddsType.observe(this, {
            tv_odds_type.text = getString(it.res)
        })

        viewModel.errorPromptMessage.observe(this, {
            it.getContentIfNotHandled()
                ?.let { message -> showErrorPromptDialog(getString(R.string.prompt), message) {} }

        })

        viewModel.leagueSelectedList.observe(this, {
            game_submit.apply {
                visibility = if (it.isEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

                text = getString(R.string.button_league_submit, it.size)
            }
        })

        viewModel.showBetInfoSingle.observe(this, {
            it?.getContentIfNotHandled()?.let {
                BetInfoCarDialog().show(supportFragmentManager, BetInfoCarDialog::class.java.simpleName)
            }
        })

        viewModel.betInfoRepository.betInfoList.observe(this, {
            sport_bottom_navigation.setBetCount(it.peekContent().size)
            canOpenBetInfoPage = it.peekContent().size > 0
        })


        viewModel.notifyLogin.observe(this, {
            snackBarLoginNotify.apply {
                setAnchorView(R.id.game_bottom_navigation)
                show()
            }
        })
    }

    private fun updateUiWithLogin(isLogin: Boolean) {
        if (isLogin) {
            btn_login.visibility = View.GONE
            btn_register.visibility = View.GONE
            toolbar_divider.visibility = View.GONE
            iv_head.visibility = View.VISIBLE
            tv_odds_type.visibility = View.VISIBLE
        } else {
            btn_login.visibility = View.VISIBLE
            btn_register.visibility = View.VISIBLE
            toolbar_divider.visibility = View.VISIBLE
            iv_head.visibility = View.GONE
            tv_odds_type.visibility = View.GONE
        }
    }

    private fun updateUiWithResult(messageListResult: MessageListResult?) {
        val titleList: MutableList<String> = mutableListOf()
        messageListResult?.let {
            it.rows?.forEach { data -> titleList.add(data.title + " - " + data.message) }

            mMarqueeAdapter.setData(titleList)

            if (messageListResult.success && titleList.size > 0) {
                rv_marquee.startAuto() //啟動跑馬燈
            } else {
                rv_marquee.stopAuto() //停止跑馬燈
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

    private fun queryData() {
        getAnnouncement()
        getSportMenu()
    }

    private fun getAnnouncement() {
        viewModel.getAnnouncement()
    }

    private fun getSportMenu() {
        loading()
        viewModel.getSportMenu()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val bundle = intent.extras
        val matchType = bundle?.getString("matchType")
        val gameType = GameType.getGameType(bundle?.getString("gameType"))

        when (matchType) {
            MatchType.PARLAY.postValue -> {
                viewModel.navSpecialEntrance(
                    SpecialEntranceSource.SHOPPING_CART,
                    MatchType.PARLAY,
                    gameType
                )
            }

            else -> {
                viewModel.navSpecialEntrance(
                    SpecialEntranceSource.SHOPPING_CART,
                    MatchType.TODAY,
                    gameType
                )
            }
        }
    }

    private fun updateSelectTabState(matchType: MatchType?) {
        when (matchType) {
            MatchType.IN_PLAY -> updateSelectTabState(1)
            MatchType.AT_START -> updateSelectTabState(2)
            MatchType.TODAY -> updateSelectTabState(3)
            MatchType.EARLY -> updateSelectTabState(4)
            MatchType.PARLAY -> updateSelectTabState(5)
            MatchType.OUTRIGHT -> updateSelectTabState(6)
            MatchType.EPS -> updateSelectTabState(7)
        }
    }

    private fun updateSelectTabState(position: Int) {
        val tab = tabLayout.getTabAt(position)?.customView

        tab?.let {
            clearSelectTabState()

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
}