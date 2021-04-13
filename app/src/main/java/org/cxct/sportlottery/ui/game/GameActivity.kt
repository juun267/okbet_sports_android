package org.cxct.sportlottery.ui.game

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.home_cate_tab.view.*
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
import org.cxct.sportlottery.ui.game.v3.GameLeagueFragment
import org.cxct.sportlottery.ui.game.v3.GameOutrightFragment
import org.cxct.sportlottery.ui.game.v3.GameV3FragmentDirections
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.ui.menu.MenuLeftFragment
import org.cxct.sportlottery.ui.odds.OddsDetailFragment
import org.cxct.sportlottery.ui.odds.OddsDetailLiveFragment
import org.cxct.sportlottery.ui.results.GameType
import org.cxct.sportlottery.util.MetricsUtil

class GameActivity : BaseNoticeActivity<GameViewModel>(GameViewModel::class) {

    private val mMarqueeAdapter by lazy { MarqueeAdapter() }
    private val mNavController by lazy { findNavController(R.id.game_container) }
    private var mCloseOddsDetail = true

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
                R.id.menu_cg_lottery -> gotToMainActivity(ThirdGameCategory.CGCP)
                R.id.menu_live_game -> gotToMainActivity(ThirdGameCategory.LIVE)
                R.id.menu_poker_game -> gotToMainActivity(ThirdGameCategory.QP)
                R.id.menu_slot_game -> gotToMainActivity(ThirdGameCategory.DZ)
                R.id.menu_fish_game -> gotToMainActivity(ThirdGameCategory.BY)
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
        refreshTabLayout(null)
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
        //規則：看當前在哪種類型賽事，再跳轉到對應類型下的球類賽事
        //若此類型賽事無該種球類比賽，則一律跳轉到“串關”類型的球類賽事
        val matchType = when (tabLayout.selectedTabPosition) {
            1 -> { //滾球盤
                val itemList = mSportMenuResult?.sportMenuData?.menu?.inPlay?.items ?: listOf()
                val targetItem = itemList.firstOrNull { it.code == sportType.code }
                if (targetItem != null) MatchType.IN_PLAY else MatchType.PARLAY
            }

            2 -> { //今日賽事
                val itemList = mSportMenuResult?.sportMenuData?.menu?.today?.items ?: listOf()
                val targetItem = itemList.firstOrNull { it.code == sportType.code }
                if (targetItem != null) MatchType.TODAY else MatchType.PARLAY
            }

            3 -> { //早盤
                val itemList = mSportMenuResult?.sportMenuData?.menu?.early?.items ?: listOf()
                val targetItem = itemList.firstOrNull { it.code == sportType.code }
                if (targetItem != null) MatchType.EARLY else MatchType.PARLAY
            }

            5 -> { //冠軍
                val itemList = mSportMenuResult?.sportMenuData?.menu?.outright?.items ?: listOf()
                val targetItem = itemList.firstOrNull { it.code == sportType.code }
                if (targetItem != null) MatchType.OUTRIGHT else MatchType.PARLAY
            }

            else -> { //全部 //串關
                MatchType.PARLAY
            }
        }
        viewModel.getGameHallList(matchType, sportType)
    }

    private fun gotToMainActivity(thirdGameCategory: ThirdGameCategory) {
        when (mNavController.currentDestination?.id) {
            R.id.homeFragment -> {
                val action = HomeFragmentDirections.actionHomeFragmentToMainActivity(thirdGameCategory)
                mNavController.navigate(action)
            }
            R.id.gameV3Fragment -> {
                val action = GameV3FragmentDirections.actionGameV3FragmentToMainActivity(thirdGameCategory)
                mNavController.navigate(action)
            }
        }
    }

    private fun initToolBar() {
        iv_logo.setImageResource(R.drawable.ic_logo)
        iv_logo.setOnClickListener {
            gotToMainActivity(ThirdGameCategory.MAIN)
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

        viewModel.isParlayPage(false)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                if (mCloseOddsDetail) {
                    popAllFragment()
                }
                viewModel.isParlayPage(tab?.position == 4)

                when (tab?.position) {
                    0 -> {
                        mNavController.popBackStack(R.id.homeFragment, false)
                    }
                    1 -> {
                        navGameFragment(MatchType.IN_PLAY)
                    }
                    2 -> {
                        navGameFragment(MatchType.TODAY)
                    }
                    3 -> {
                        navGameFragment(MatchType.EARLY)
                    }
                    4 -> {
                        navGameFragment(MatchType.PARLAY)
                    }
                    5 -> {
                        navGameFragment(MatchType.OUTRIGHT)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                popAllFragment()
                if (tab?.position == 0) {
                    val tabView = tabLayout.getTabAt(0)?.customView
                    tabView?.tv_title?.isSelected = true
                    tabView?.tv_number?.isSelected = true
                    mNavController.popBackStack(R.id.homeFragment, false)
                }
            }
        })
    }

    private fun navGameFragment(matchType: MatchType) {

        viewModel.sportMenuSelectFirstItem(matchType)

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
                mNavController.popBackStack(R.id.gameV3Fragment, false)
            }
        }
    }

    private fun addFragment(fragment: Fragment, page: Page) {
        if (supportFragmentManager.findFragmentByTag(page.name) == null) {
            supportFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.enter_from_right,
                0
            ).add(
                R.id.odds_detail_container,
                fragment,
                page.name
            ).addToBackStack(
                page.name
            ).commit()
        }
    }


    override fun onBackPressed() {
        if (mNavController.currentDestination?.id == R.id.gameLeagueFragment) {
            mNavController.navigateUp()
            return
        }

        if (mNavController.currentDestination?.id != R.id.homeFragment && supportFragmentManager.backStackEntryCount == 0) {
            tabLayout.getTabAt(0)?.select()
            return
        }

        super.onBackPressed()
    }

    private fun initObserve() {
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

        viewModel.curOddsDetailParams.observe(this, Observer {
//            //TODO simon test 從首頁滾球盤跳轉到投注詳情頁面，back 時要直接回到首頁
//            tabLayout.getTabAt(tabLayout.selectedTabPosition)?.customView?.isSelected = false
//            tabLayout.getTabAt(1)?.customView?.isSelected = true
//            tabLayout.getTabAt(1)?.select()

            val gameType = it[0]
            val typeName = it[1]
            val matchId = it[2] ?: ""
            val oddsType = "EU"

            app_bar_layout.setExpanded(true, true)

            addFragment(
                OddsDetailFragment.newInstance(gameType, typeName, matchId, oddsType),
                Page.ODDS_DETAIL
            )
        })

        viewModel.curOddsDetailLiveParams.observe(this, {
            val gameType = it[0]
            val typeName = it[1]
            val matchId = it[2] ?: ""
            val oddsType = "EU"

            app_bar_layout.setExpanded(true, true)

            addFragment(
                OddsDetailLiveFragment.newInstance(gameType, typeName, matchId, oddsType),
                Page.ODDS_DETAIL_LIVE
            )
        })

        viewModel.matchTypeCardForParlay.observe(this, {
            app_bar_layout.setExpanded(true, false)
            when (it) {
                MatchType.PARLAY -> {
                    tabLayout.getTabAt(4)?.select()
                }
                MatchType.AT_START -> {
                    toAtStart()
                }
                MatchType.IN_PLAY -> {
                    tabLayout.getTabAt(1)?.select()
                }
                else -> {
                }
            }
        })

        viewModel.openOutrightDetail.observe(this, Observer {
            app_bar_layout.setExpanded(true, true)
            addFragment(GameOutrightFragment.newInstance(it.first, it.second), Page.OUTRIGHT)
        })

        viewModel.userInfo.observe(this, {
            updateAvatar(it?.iconUrl)
        })
    }

    private fun updateUiWithLogin(isLogin: Boolean) {
        if (isLogin) {
            btn_login.visibility = View.GONE
            btn_register.visibility = View.GONE
            toolbar_divider.visibility = View.GONE
            iv_head.visibility = View.VISIBLE
        } else {
            btn_login.visibility = View.VISIBLE
            btn_register.visibility = View.VISIBLE
            toolbar_divider.visibility = View.VISIBLE
            iv_head.visibility = View.GONE
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
    }

    private fun getAnnouncement() {
        viewModel.getAnnouncement()
    }

    private fun getSportMenu() {
        loading()
        viewModel.getSportMenu()
    }

    private fun popAllFragment() {
        val manager = supportFragmentManager
        for (i in 0 until manager.backStackEntryCount) {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val bundle = intent.extras
        val gameTypeList = GameType.values()
        val typeName = gameTypeList.find { it.key == bundle?.getString("gameType") }?.string
        val gameType = bundle?.getString("gameType")
        val matchId = bundle?.getString("matchId")

        val fragment: Fragment? = supportFragmentManager.findFragmentByTag(Page.ODDS_DETAIL.name)
        if (fragment != null) {
            mCloseOddsDetail = false
            (fragment as OddsDetailFragment).refreshData(gameType, matchId)
        } else {
            viewModel.getOddsDetail(gameType, typeName?.let { getString(it) }, matchId)
        }

        when (bundle?.getString("matchType")) {
            null, MatchType.IN_PLAY.postValue -> tabLayout.getTabAt(1)?.select()
            MatchType.TODAY.postValue -> tabLayout.getTabAt(2)?.select()
            MatchType.EARLY.postValue -> tabLayout.getTabAt(3)?.select()
            MatchType.PARLAY.postValue -> tabLayout.getTabAt(4)?.select()
            MatchType.OUTRIGHT.postValue -> tabLayout.getTabAt(5)?.select()
            MatchType.AT_START.postValue -> toAtStart()
        }
        mCloseOddsDetail = true
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