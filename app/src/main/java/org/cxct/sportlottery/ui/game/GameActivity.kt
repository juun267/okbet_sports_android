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
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.home_cate_tab.view.*
import kotlinx.android.synthetic.main.view_message.*
import kotlinx.android.synthetic.main.view_nav_right.*
import kotlinx.android.synthetic.main.view_toolbar_main.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityGameBinding
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.ui.MarqueeAdapter
import org.cxct.sportlottery.ui.base.BaseNoticeActivity
import org.cxct.sportlottery.ui.game.outright.OutrightDetailFragment
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.ui.odds.OddsDetailFragment
import org.cxct.sportlottery.ui.results.GameType
import org.cxct.sportlottery.ui.splash.SplashViewModel
import org.cxct.sportlottery.util.MetricsUtil
import org.koin.androidx.viewmodel.ext.android.viewModel

class GameActivity : BaseNoticeActivity<GameViewModel>(GameViewModel::class) {

    private val mSplashViewModel: SplashViewModel by viewModel()

    private lateinit var mainBinding: ActivityGameBinding

    private val mMarqueeAdapter = MarqueeAdapter()

    enum class Page {
        ODDS_DETAIL, ODDS, OUTRIGHT
    }

    private val navController by lazy { findNavController(R.id.game_container) }

    private var closeOddsDetail = true

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

        //若啟動頁是使用 local host 進入，到首頁要再 getHost() 一次，背景替換使用最快線路
        if (mSplashViewModel.isNeedGetHost())
            mSplashViewModel.getHost()
    }

    override fun onResume() {
        super.onResume()
        rv_marquee.startAuto()
    }

    override fun onPause() {
        super.onPause()
        rv_marquee.stopAuto()
    }

    private fun initToolBar() {
        iv_logo.setImageResource(R.drawable.ic_logo)
        iv_logo.setOnClickListener {
            tabLayout.getTabAt(0)?.select()
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
            val menuFrag =
                supportFragmentManager.findFragmentById(R.id.fragment_menu) as MenuFragment
            menuFrag.setDownMenuListener(View.OnClickListener { drawer_layout.closeDrawers() })

            nav_right.layoutParams.width = MetricsUtil.getMenuWidth() //動態調整側邊欄寬
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
            val countAsStart = sportMenuResult?.sportMenuData?.atStart?.items?.sumBy { it.num } ?: 0

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

                if (closeOddsDetail) {
                    popAllFragment()
                }
                viewModel.isParlayPage(tab?.position == 4)

                when (tab?.position) {
                    0 -> {
                        navController.popBackStack(R.id.homeFragment, false)
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
                    navController.popBackStack(R.id.homeFragment, false)
                }
            }
        })
    }

    private fun navGameFragment(matchType: MatchType) {

        viewModel.sportMenuSelectFirstItem(matchType)

        when (navController.currentDestination?.id) {
            R.id.homeFragment -> {
                val action = HomeFragmentDirections.actionHomeFragmentToGameFragment(matchType)
                navController.navigate(action)
            }
            R.id.gameFragment -> {
                val action = GameFragmentDirections.actionGameFragmentToGameFragment(matchType)
                val navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
                navController.navigate(action, navOptions)
            }
            R.id.game2Fragment -> {
                val action =
                    GameDetailFragmentDirections.actionGame2FragmentToGameFragment(matchType)
                val navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()
                navController.navigate(action, navOptions)
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
        if (navController.currentDestination?.id != R.id.homeFragment && supportFragmentManager.backStackEntryCount == 0) {
            tabLayout.getTabAt(0)?.select()
            return
        }

        super.onBackPressed()
    }

    private fun initObserve() {
        viewModel.isLogin.observe(this, Observer {
            updateUiWithLogin(it)
        })

        viewModel.messageListResult.observe(this, Observer {
            hideLoading()
            updateUiWithResult(it)
        })

        viewModel.sportMenuResult.observe(this, Observer {
            hideLoading()
            updateUiWithResult(it)
        })

        viewModel.curOddsDetailParams.observe(this, Observer {
            val gameType = it[0]
            val typeName = it[1]
            val matchId = it[2] ?: ""
            val oddsType = "EU"

            getAppBarLayout().setExpanded(true, true)

            addFragment(
                OddsDetailFragment.newInstance(gameType, typeName, matchId, oddsType),
                Page.ODDS_DETAIL
            )
        })

        viewModel.matchTypeCardForParlay.observe(this, Observer {
            when (it) {
                MatchType.PARLAY -> {
                    tabLayout.getTabAt(4)?.select()
                }
                MatchType.AT_START -> {
                    toAtStart()
                }
                else -> {
                }
            }
        })

        viewModel.openGameDetail.observe(this, Observer {
            getAppBarLayout().setExpanded(true, true)
            addFragment(GameDetailFragment.newInstance(it.second, it.first), Page.ODDS)

        })

        viewModel.openOutrightDetail.observe(this, Observer {
            getAppBarLayout().setExpanded(true, true)
            addFragment(OutrightDetailFragment.newInstance(it.second, it.first), Page.OUTRIGHT)
        })

        viewModel.userInfo.observe(this, Observer {
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

    private fun updateUiWithResult(messageListResult: MessageListResult) {
        val titleList: MutableList<String> = mutableListOf()
        messageListResult.rows?.forEach { data -> titleList.add(data.title + " - " + data.message) }

        if (messageListResult.success && titleList.size > 0) {
            rv_marquee.startAuto() //啟動跑馬燈
        } else {
            rv_marquee.stopAuto() //停止跑馬燈
        }

        mMarqueeAdapter.setData(titleList)
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

    fun getAppBarLayout(): AppBarLayout {
        return mainBinding.appBarLayout
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
            closeOddsDetail = false
            (fragment as OddsDetailFragment).refreshData(
                gameType,
                matchId,
                typeName?.let { getString(it) })
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
        closeOddsDetail = true
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