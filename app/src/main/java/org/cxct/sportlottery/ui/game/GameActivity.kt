package org.cxct.sportlottery.ui.game

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_game.drawer_layout
import kotlinx.android.synthetic.main.activity_game.tabLayout
import kotlinx.android.synthetic.main.home_cate_tab.view.*
import kotlinx.android.synthetic.main.toast_top_bet_result.*
import kotlinx.android.synthetic.main.view_message.*
import kotlinx.android.synthetic.main.view_message.rv_marquee
import kotlinx.android.synthetic.main.view_nav_right.*
import kotlinx.android.synthetic.main.view_nav_right.nav_right
import kotlinx.android.synthetic.main.view_toolbar_main.*
import kotlinx.android.synthetic.main.view_toolbar_main.btn_login
import kotlinx.android.synthetic.main.view_toolbar_main.btn_register
import kotlinx.android.synthetic.main.view_toolbar_main.iv_head
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.ui.MarqueeAdapter
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity
import org.cxct.sportlottery.ui.home.HomeFragmentDirections
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.util.MetricsUtil

class GameActivity : BaseOddButtonActivity<GameViewModel>(GameViewModel::class) {

    private val navController by lazy {
        findNavController(R.id.homeFragment)
    }

    private val mMarqueeAdapter by lazy {
        MarqueeAdapter()
    }

    private val tabAll by lazy {
        tabLayout.getTabAt(0)?.customView
    }
    private val tabInPlay by lazy {
        tabLayout.getTabAt(1)?.customView
    }
    private val tabToday by lazy {
        tabLayout.getTabAt(2)?.customView
    }
    private val tabEarly by lazy {
        tabLayout.getTabAt(3)?.customView
    }
    private val tabParlay by lazy {
        tabLayout.getTabAt(4)?.customView
    }
    private val tabOutright by lazy {
        tabLayout.getTabAt(5)?.customView
    }
    private val tabAtStart by lazy {
        tabLayout.getTabAt(6)?.customView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        setupToolbar()
        setupDrawer()
        setupMessage()
        setupTabLayout()

        initObserver()
    }

    override fun onResume() {
        super.onResume()

        rv_marquee.startAuto()
    }

    override fun onPause() {
        super.onPause()

        rv_marquee.stopAuto()
    }

    private fun setupToolbar() {
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

    private fun setupDrawer() {
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

    private fun setupMessage() {
        rv_marquee.apply {
            layoutManager =
                LinearLayoutManager(this@GameActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = mMarqueeAdapter
        }
    }

    private fun setupTabLayout() {
        tabAll?.tv_title?.setText(R.string.home_tab_all)
        tabInPlay?.tv_title?.setText(R.string.home_tab_in_play)
        tabToday?.tv_title?.setText(R.string.home_tab_today)
        tabEarly?.tv_title?.setText(R.string.home_tab_early)
        tabParlay?.tv_title?.setText(R.string.home_tab_parlay)
        tabOutright?.tv_title?.setText(R.string.home_tab_outright)
        tabAtStart?.visibility = View.GONE

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectTab(tab?.position ?: 0)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                selectTab(tab?.position ?: 0)
            }
        })
    }

    private fun selectTab(position: Int) {
        popAllFragment()

        when (position) {
            0 -> viewModel.selectMatchType(null)
            1 -> viewModel.selectMatchType(MatchType.IN_PLAY)
            2 -> viewModel.selectMatchType(MatchType.TODAY)
            3 -> viewModel.selectMatchType(MatchType.EARLY)
            4 -> viewModel.selectMatchType(MatchType.PARLAY)
            5 -> viewModel.selectMatchType(MatchType.OUTRIGHT)
        }
    }

    private fun popAllFragment() {
        for (i in 0 until supportFragmentManager.backStackEntryCount) {
            supportFragmentManager.popBackStack()
        }
    }

    private fun initObserver() {
        viewModel.isLogin.observe(this, Observer {
            updateLoginWidget(it)

            viewModel.getMessage()
            viewModel.getSportMenu()
            loading()
        })

        viewModel.messageListResult.observe(this, Observer {
            hideLoading()
            updateMessage(it)
        })

        viewModel.countInPlay.observe(this, Observer {
            hideLoading()
            tabInPlay?.tv_number?.text = it.toString()
        })

        viewModel.countToday.observe(this, Observer {
            hideLoading()
            tabToday?.tv_number?.text = it.toString()
        })

        viewModel.countEarly.observe(this, Observer {
            hideLoading()
            tabEarly?.tv_number?.text = it.toString()
        })

        viewModel.countParlay.observe(this, Observer {
            hideLoading()
            tabParlay?.tv_number?.text = it.toString()
        })

        viewModel.countOutright.observe(this, Observer {
            hideLoading()
            tabOutright?.tv_number?.text = it.toString()
        })

        viewModel.countAll.observe(this, Observer {
            hideLoading()
            tabAll?.tv_number?.text = it.toString()
        })

        viewModel.sportMenuResult.observe(this, Observer {
            updateTabLayout(it)
        })
    }

    private fun updateLoginWidget(isLogin: Boolean) {
        when (isLogin) {
            true -> {
                btn_login.visibility = View.GONE
                btn_register.visibility = View.GONE
                toolbar_divider.visibility = View.GONE
                iv_head.visibility = View.VISIBLE
            }
            false -> {
                btn_login.visibility = View.VISIBLE
                btn_register.visibility = View.VISIBLE
                toolbar_divider.visibility = View.VISIBLE
                iv_head.visibility = View.GONE
            }
        }
    }

    private fun updateMessage(messageList: List<String>) {
        mMarqueeAdapter.setData(messageList.toMutableList())

        if (messageList.isEmpty()) {
            rv_marquee.stopAuto()
        } else {
            rv_marquee.startAuto()
        }
    }

    private fun updateTabLayout(sportMenuResult: SportMenuResult) {
        tabInPlay?.isSelected = sportMenuResult.sportMenuData?.menu?.inPlay?.isSelect ?: false
        tabToday?.isSelected = sportMenuResult.sportMenuData?.menu?.today?.isSelect ?: false
        tabEarly?.isSelected = sportMenuResult.sportMenuData?.menu?.early?.isSelect ?: false
        tabParlay?.isSelected = sportMenuResult.sportMenuData?.menu?.parlay?.isSelect ?: false
        tabOutright?.isSelected = sportMenuResult.sportMenuData?.menu?.outright?.isSelect ?: false
        tabAtStart?.isSelected = sportMenuResult.sportMenuData?.atStart?.isSelect ?: false
        tabAll?.isSelected = !(tabInPlay?.isSelected ?: false) && !(tabToday?.isSelected
            ?: false) && !(tabEarly?.isSelected ?: false) && !(tabParlay?.isSelected
            ?: false) && !(tabOutright?.isSelected ?: false) && !(tabAtStart?.isSelected ?: false)

        when {
            tabInPlay?.isSelected ?: false -> {
                navGameFragment(MatchType.IN_PLAY)
            }
            tabToday?.isSelected ?: false -> {
                navGameFragment(MatchType.TODAY)
            }
            tabEarly?.isSelected ?: false -> {
                navGameFragment(MatchType.EARLY)
            }
            tabParlay?.isSelected ?: false -> {
                navGameFragment(MatchType.PARLAY)
            }
            tabOutright?.isSelected ?: false -> {
                navGameFragment(MatchType.OUTRIGHT)
            }
            tabAtStart?.isSelected ?: false -> {
                navGameFragment(MatchType.AT_START)
            }
            tabAll?.isSelected ?: false -> {
                navController.popBackStack(R.id.homeFragment, false)
            }
        }
    }

    private fun navGameFragment(matchType: MatchType) {
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
}