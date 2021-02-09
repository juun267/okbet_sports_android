package org.cxct.sportlottery.ui.home

import android.content.*
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.home_cate_tab.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityMainBinding
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.message.MessageListResult
import org.cxct.sportlottery.network.sport.SportMenuResult
import org.cxct.sportlottery.ui.MarqueeAdapter
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity
import org.cxct.sportlottery.ui.game.GameDetailFragment
import org.cxct.sportlottery.ui.game.GameDetailFragmentDirections
import org.cxct.sportlottery.ui.game.GameFragmentDirections
import org.cxct.sportlottery.ui.game.outright.OutrightDetailFragment
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.ui.odds.OddsDetailFragment
import org.cxct.sportlottery.util.MetricsUtil

class MainActivity : BaseOddButtonActivity<MainViewModel>(MainViewModel::class) {

    companion object {
        //切換語系，activity 要重啟才會生效
        fun reStart(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    private lateinit var mainBinding: ActivityMainBinding

    private val mMarqueeAdapter = MarqueeAdapter()

    enum class Page {
        ODDS_DETAIL, ODDS, OUTRIGHT
    }

    private val navController by lazy {
        findNavController(R.id.homeFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mainBinding.apply {
            mainViewModel = this@MainActivity.viewModel
            lifecycleOwner = this@MainActivity
        }

        initToolBar()
        initMenu()
        initRvMarquee()
        refreshTabLayout(null)
        initObserve()
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

        //頭像 當 側邊欄 開/關
        iv_head.setOnClickListener {
            if (drawer_layout.isDrawerOpen(nav_right)) drawer_layout.closeDrawers()
            else {
                drawer_layout.openDrawer(nav_right)
            }
        }

        btn_login.setOnClickListener {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        }

        btn_register.setOnClickListener {
            startActivity(Intent(this@MainActivity, RegisterActivity::class.java))
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
            tabAll?.tv_number?.text =
                (countInPlay + countToday + countEarly + countParlay + countAsStart).toString()

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

            val tabAtStart = tabLayout.getTabAt(6)?.customView
            tabAtStart?.visibility = View.GONE

        } catch (e: Exception) {
            e.printStackTrace()
        }

        viewModel.isParlayPage(false)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                popAllFragment()
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
                    6 -> {
                        navGameFragment(MatchType.AT_START)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
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
            queryData()
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

            backService.subscribeEventChannel(matchId)
        })

        viewModel.matchTypeCardForParlay.observe(this, Observer {
            when (it) {
                MatchType.PARLAY -> {
                    tabLayout.getTabAt(4)?.select()
                }
                MatchType.AT_START -> {
                    tabLayout.getTabAt(6)?.select()
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

        viewModel.errorResultToken.observe(this, Observer {
            viewModel.logout()
        })

        viewModel.userInfo.observe(this, Observer {
            updateAvatar(it?.iconUrl)
        })

        receiver.userNotice.observe(this, Observer {
            //TODO simon test review UserNotice 彈窗，需要顯示在最上層，目前如果開啟多個 activity，現行架構只會顯示在 MainActivity 裡面
            it?.userNoticeList?.let { list ->
                if (list.isNotEmpty())
                    UserNoticeDialog(this).setNoticeList(list).show()
            }
        })

        receiver.notice.observe(this, Observer {
            hideLoading()
            if (it != null) {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUiWithResult(messageListResult: MessageListResult) {
        val titleList: MutableList<String> = mutableListOf()
        messageListResult.rows?.forEach { data -> titleList.add(data.title + " - " + data.content) }

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
        Glide.with(this).load(iconUrl).apply(RequestOptions().placeholder(R.drawable.ic_head)).into(
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

}
