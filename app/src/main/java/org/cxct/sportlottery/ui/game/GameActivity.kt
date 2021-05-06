package org.cxct.sportlottery.ui.game

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.home_cate_tab.view.*
import kotlinx.android.synthetic.main.toast_top_bet_result.*
import kotlinx.android.synthetic.main.view_message.*
import kotlinx.android.synthetic.main.view_nav_left.*
import kotlinx.android.synthetic.main.view_nav_left.view.*
import kotlinx.android.synthetic.main.view_nav_right.*
import kotlinx.android.synthetic.main.view_toolbar_main.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.ui.MarqueeAdapter
import org.cxct.sportlottery.ui.base.BaseNoticeActivity
import org.cxct.sportlottery.ui.game.home.HomeFragmentDirections
import org.cxct.sportlottery.ui.game.v3.*
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.MainActivity.Companion.ARGS_THIRD_GAME_CATE
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.menu.ChangeOddsTypeDialog
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.ui.menu.MenuLeftFragment
import org.cxct.sportlottery.ui.odds.OddsDetailFragmentDirections
import org.cxct.sportlottery.ui.odds.OddsDetailLiveFragmentDirections
import org.cxct.sportlottery.util.MetricsUtil


class GameActivity : BaseNoticeActivity<GameViewModel>(GameViewModel::class) {

    private val mMarqueeAdapter by lazy { MarqueeAdapter() }
    private val mNavController by lazy { findNavController(R.id.game_container) }

    private var mSportMenuResult: SportMenuResult? = null
    private val mMenuLeftListener = object : MenuLeftFragment.MenuLeftListener {
        override fun onClick(id: Int) {
            when (id) {
                R.id.btn_lobby -> iv_logo.performClick()
                R.id.menu_sport_game -> tabLayout.getTabAt(0)?.select()
                R.id.menu_in_play -> tabLayout.getTabAt(1)?.select()
                R.id.menu_date_row_today -> tabLayout.getTabAt(2)?.select()
                R.id.menu_early -> tabLayout.getTabAt(3)?.select()
                R.id.menu_parlay -> tabLayout.getTabAt(4)?.select()
                R.id.menu_champion -> tabLayout.getTabAt(5)?.select()
                R.id.menu_soccer -> goToSportGame(SportType.FOOTBALL)
                R.id.menu_basketball -> goToSportGame(SportType.BASKETBALL)
                R.id.menu_tennis -> goToSportGame(SportType.TENNIS)
                R.id.menu_badminton -> goToSportGame(SportType.BADMINTON)
                R.id.menu_volleyball -> goToSportGame(SportType.VOLLEYBALL)
                R.id.menu_cg_lottery -> goToMainActivity(ThirdGameCategory.CGCP)
                R.id.menu_live_game -> goToMainActivity(ThirdGameCategory.LIVE)
                R.id.menu_poker_game -> goToMainActivity(ThirdGameCategory.QP)
                R.id.menu_slot_game -> goToMainActivity(ThirdGameCategory.DZ)
                R.id.menu_fish_game -> goToMainActivity(ThirdGameCategory.BY)
            }
        }
    }

    enum class Page { ODDS_DETAIL, ODDS, OUTRIGHT, ODDS_DETAIL_LIVE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        setupNoticeButton(btn_notice)
        initToolBar()
        initMenu()
        initRvMarquee()
        initTabLayout()
        initObserve()

        queryData()
    }

    override fun onResume() {
        super.onResume()
        rv_marquee.startAuto()
    }

    override fun onPause() {
        super.onPause()
        rv_marquee.stopAuto()
    }

    private fun goToSportGame(sportType: SportType) {
        //規則：
        //1. 優先跳轉到當前頁籤下選擇要跳轉的球類賽事
        //2. 若此當前頁籤無該種球類比賽，則跳轉到"今日"頁籤下的對應球類賽事
        //3. 若"今日"也沒有則跳到"串關"
        //4. 若扔沒有則顯示無賽事的圖片

        val todayItemList = mSportMenuResult?.sportMenuData?.menu?.today?.items ?: listOf()
        val todayItem = todayItemList.firstOrNull { it.code == sportType.code }
        val matchType = when (tabLayout.selectedTabPosition) {
            1 -> { //滾球盤
                val itemList = mSportMenuResult?.sportMenuData?.menu?.inPlay?.items ?: listOf()
                val targetItem = itemList.firstOrNull { it.code == sportType.code }
                when {
                    targetItem != null -> MatchType.IN_PLAY
                    todayItem != null -> MatchType.TODAY
                    else -> MatchType.PARLAY
                }
            }

            2 -> { //今日
                when {
                    todayItem != null -> MatchType.TODAY
                    else -> MatchType.PARLAY
                }
            }

            3 -> { //早盤
                val itemList = mSportMenuResult?.sportMenuData?.menu?.early?.items ?: listOf()
                val targetItem = itemList.firstOrNull { it.code == sportType.code }
                when {
                    targetItem != null -> MatchType.EARLY
                    todayItem != null -> MatchType.TODAY
                    else -> MatchType.PARLAY
                }
            }

            4 -> { //串關
                MatchType.PARLAY
            }

            5 -> { //冠軍
                val itemList = mSportMenuResult?.sportMenuData?.menu?.outright?.items ?: listOf()
                val targetItem = itemList.firstOrNull { it.code == sportType.code }
                when {
                    targetItem != null -> MatchType.OUTRIGHT
                    todayItem != null -> MatchType.TODAY
                    else -> MatchType.PARLAY
                }
            }

            else -> { //全部
                when {
                    todayItem != null -> MatchType.TODAY
                    else -> MatchType.PARLAY
                }
            }
        }
        viewModel.getGameHallList(matchType, sportType, true)
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
            val menuFrag = supportFragmentManager.findFragmentById(R.id.fragment_menu) as MenuFragment
            menuFrag.setDownMenuListener { drawer_layout.closeDrawers() }
            nav_right.layoutParams.width = MetricsUtil.getMenuWidth() //動態調整側邊欄寬

            //選單選擇結束要收起選單
            val menuLeftFrag = supportFragmentManager.findFragmentById(R.id.fragment_menu_left) as MenuLeftFragment
            menuLeftFrag.setDownMenuListener { drawer_layout.closeDrawers() }
            menuLeftFrag.setMenuLeftListener(mMenuLeftListener)
            nav_left.layoutParams.width = MetricsUtil.getMenuWidth() //動態調整側邊欄寬

            btn_menu_left.setOnClickListener {
                if (drawer_layout.isDrawerOpen(nav_left)) drawer_layout.closeDrawers()
                else {
                    drawer_layout.openDrawer(nav_left)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
            val countToday =
                sportMenuResult?.sportMenuData?.menu?.today?.items?.sumBy { it.num } ?: 0
            val countEarly =
                sportMenuResult?.sportMenuData?.menu?.early?.items?.sumBy { it.num } ?: 0
            val countParlay =
                sportMenuResult?.sportMenuData?.menu?.parlay?.items?.sumBy { it.num } ?: 0
            val countOutright =
                sportMenuResult?.sportMenuData?.menu?.outright?.items?.sumBy { it.num } ?: 0

            val tabAll = tabLayout.getTabAt(0)?.customView
            tabAll?.tv_title?.setText(R.string.home_tab_all)
            tabAll?.tv_number?.text = countParlay.toString() //等於串關數量

            val tabInPlay = tabLayout.getTabAt(1)?.customView
            tabInPlay?.tv_title?.setText(R.string.home_tab_in_play)
            tabInPlay?.tv_number?.text = countInPlay.toString()

            val tabToday = tabLayout.getTabAt(2)?.customView
            tabToday?.tv_title?.setText(R.string.home_tab_today)
            tabToday?.tv_number?.text = countToday.toString()

            val tabEarly = tabLayout.getTabAt(3)?.customView
            tabEarly?.tv_title?.setText(R.string.home_tab_early)
            tabEarly?.tv_number?.text = countEarly.toString()

            val tabParlay = tabLayout.getTabAt(4)?.customView
            tabParlay?.tv_title?.setText(R.string.home_tab_parlay)
            tabParlay?.tv_number?.text = countParlay.toString()

            val tabOutright = tabLayout.getTabAt(5)?.customView
            tabOutright?.tv_title?.setText(R.string.home_tab_outright)
            tabOutright?.tv_number?.text = countOutright.toString()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun selectTab(position: Int?) {
        viewModel.isParlayPage(position == 4)

        when (position) {
            0 -> {
                mNavController.popBackStack(R.id.homeFragment, false)
            }
            1 -> {
                viewModel.getSportMenu(MatchType.IN_PLAY)
                loading()
            }
            2 -> {
                viewModel.getSportMenu(MatchType.TODAY)
                loading()
            }
            3 -> {
                viewModel.getSportMenu(MatchType.EARLY)
                loading()
            }
            4 -> {
                viewModel.getSportMenu(MatchType.PARLAY)
                loading()
            }
            5 -> {
                viewModel.getSportMenu(MatchType.OUTRIGHT)
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
        when (mNavController.currentDestination?.id) {
            R.id.gameLeagueFragment, R.id.gameOutrightFragment, R.id.oddsDetailFragment, R.id.oddsDetailLiveFragment -> {
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
        receiver.orderSettlement.observe(this, {
            viewModel.getSettlementNotification(it)
        })

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

        viewModel.sportMenuResult.observe(this, {
            hideLoading()
            updateUiWithResult(it)
        })

        viewModel.matchTypeCardForParlay.observe(this, {
            val matchType = it?.getContentIfNotHandled()?.first

            app_bar_layout.setExpanded(true, false)

            when (matchType) {
                MatchType.IN_PLAY -> {
                    tabLayout.getTabAt(1)?.select()
                }
                MatchType.TODAY -> {
                    tabLayout.getTabAt(2)?.select()
                }
                MatchType.EARLY -> {
                    tabLayout.getTabAt(3)?.select()
                }
                MatchType.PARLAY -> {
                    tabLayout.getTabAt(4)?.select()
                }
                MatchType.OUTRIGHT -> {
                    tabLayout.getTabAt(5)?.select()
                }
                MatchType.AT_START -> {
                    toAtStart()
                }
            }
        })

        viewModel.userInfo.observe(this, {
            updateAvatar(it?.iconUrl)
        })

        viewModel.systemDelete.observe(this, {
            if (it) {
                showErrorPromptDialog(getString(R.string.prompt), getString(R.string.bet_info_system_close_incompatible_item)) {}
            }
        })

        viewModel.oddsType.observe(this, {
            tv_odds_type.text = getString(it.res)
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

            sportMenuResult.sportMenuData?.matchType?.let {
                navGameFragment(it)
            }
        }

        mSportMenuResult = sportMenuResult
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
        defaultPage()
    }

    private fun getAnnouncement() {
        viewModel.getAnnouncement()
    }

    private fun getSportMenu() {
        loading()
        viewModel.getSportMenu()
    }

    private fun defaultPage() {
        viewModel.isParlayPage(false)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val bundle = intent.extras
        val matchType = bundle?.getString("matchType")
        val gameType = bundle?.getString("gameType")
        val matchId = bundle?.getString("matchId")

        val sportType = when (gameType) {
            SportType.BASKETBALL.code -> SportType.BASKETBALL
            SportType.TENNIS.code -> SportType.TENNIS
            SportType.BADMINTON.code -> SportType.BADMINTON
            SportType.VOLLEYBALL.code -> SportType.VOLLEYBALL
            SportType.FOOTBALL.code -> SportType.FOOTBALL
            else -> null
        }

        when (matchType) {
            MatchType.IN_PLAY.postValue -> {
                tabLayout.getTabAt(1)?.select()

                sportType?.let {
                    matchId?.let {
                        navOddsDetailLiveFragment(sportType, matchId)
                    }
                }
            }

            MatchType.TODAY.postValue -> {
                tabLayout.getTabAt(2)?.select()

                sportType?.let {
                    matchId?.let {
                        navOddsDetailFragment(MatchType.TODAY, sportType, matchId)
                    }
                }
            }

            MatchType.EARLY.postValue -> {
                tabLayout.getTabAt(3)?.select()

                sportType?.let {
                    matchId?.let {
                        navOddsDetailFragment(MatchType.EARLY, sportType, matchId)
                    }
                }
            }

            MatchType.PARLAY.postValue -> {
                tabLayout.getTabAt(4)?.select()

                sportType?.let {
                    matchId?.let {
                        navOddsDetailFragment(MatchType.PARLAY, sportType, matchId)
                    }
                }
            }

            MatchType.AT_START.postValue -> {
                toAtStart()

                sportType?.let {
                    matchId?.let {
                        navOddsDetailFragment(MatchType.AT_START, sportType, matchId)
                    }
                }
            }

            MatchType.OUTRIGHT.postValue -> tabLayout.getTabAt(5)?.select()
        }
    }

    private fun navOddsDetailFragment(matchType: MatchType, sportType: SportType, matchId: String) {
        val action = when (mNavController.currentDestination?.id) {
            R.id.gameV3Fragment -> {
                GameV3FragmentDirections.actionGameV3FragmentToOddsDetailFragment(
                    matchType,
                    sportType,
                    matchId
                )
            }
            R.id.gameLeagueFragment -> {
                GameLeagueFragmentDirections.actionGameLeagueFragmentToOddsDetailFragment(
                    matchType,
                    sportType,
                    matchId
                )
            }
            R.id.oddsDetailLiveFragment -> {
                OddsDetailFragmentDirections.actionOddsDetailFragmentToOddsDetailLiveFragment(
                    sportType,
                    matchId
                )
            }
            R.id.oddsDetailFragment -> {
                OddsDetailFragmentDirections.actionOddsDetailFragmentSelf(sportType, matchId, matchType)
            }
            else -> null
        }

        action?.let {
            mNavController.navigate(action)
        }
    }

    private fun navOddsDetailLiveFragment(sportType: SportType, matchId: String) {
        val action = when (mNavController.currentDestination?.id) {
            R.id.gameV3Fragment -> {
                GameV3FragmentDirections.actionGameV3FragmentToOddsDetailLiveFragment(
                    sportType,
                    matchId
                )
            }
            R.id.gameLeagueFragment -> {
                GameLeagueFragmentDirections.actionGameLeagueFragmentToOddsDetailLiveFragment(
                    sportType,
                    matchId
                )
            }
            R.id.oddsDetailFragment -> {
                OddsDetailFragmentDirections.actionOddsDetailFragmentToOddsDetailLiveFragment(
                    sportType,
                    matchId
                )
            }
            R.id.oddsDetailLiveFragment -> {
                OddsDetailLiveFragmentDirections.actionOddsDetailLiveFragmentSelf(
                    sportType,
                    matchId
                )
            }
            else -> null
        }

        action?.let {
            mNavController.navigate(action)
        }
    }

    private fun nonSelectTab() {
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)?.customView
            tab?.tv_title?.isSelected = false
            tab?.tv_number?.isSelected = false
        }
    }

    private fun toAtStart() {
        nonSelectTab()
        navGameFragment(MatchType.AT_START)
    }

}