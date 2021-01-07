package org.cxct.sportlottery.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
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
import org.cxct.sportlottery.repository.sLoginData
import org.cxct.sportlottery.ui.MarqueeAdapter
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.game.Game2FragmentDirections
import org.cxct.sportlottery.ui.game.GameFragmentDirections
import org.cxct.sportlottery.ui.login.LoginActivity
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.ui.odds.OddsDetailFragment
import org.cxct.sportlottery.util.MetricsUtil

class MainActivity : BaseActivity<MainViewModel>(MainViewModel::class) {

    companion object {
        private const val TAG = "MainActivity"

        //切換語系，activity 要重啟才會生效
        fun reStart(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    private lateinit var mainBinding: ActivityMainBinding

    private val mMarqueeAdapter = MarqueeAdapter()

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
            if (drawer_layout.isDrawerOpen(nav_right))
                drawer_layout.closeDrawers()
            else {
                drawer_layout.openDrawer(nav_right)
            }
        }

        btn_login.setOnClickListener {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        }

        btn_register.setOnClickListener {
            //TODO simon test 跳轉註冊頁面
//            viewModel.logout()
//            getAnnouncement()
        }
    }

    private fun initMenu() {
        try {
            //關閉側邊欄滑動行為
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            //選單選擇結束要收起選單
            val menuFrag = supportFragmentManager.findFragmentById(R.id.fragment_menu) as MenuFragment
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
            val countInPlay = sportMenuResult?.sportMenuData?.menu?.inPlay?.items?.sumBy { it.num } ?: 0
            val countToday = sportMenuResult?.sportMenuData?.menu?.today?.items?.sumBy { it.num } ?: 0
            val countEarly = sportMenuResult?.sportMenuData?.menu?.early?.items?.sumBy { it.num } ?: 0
            val countParlay = sportMenuResult?.sportMenuData?.menu?.parlay?.items?.sumBy { it.num } ?: 0
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
        } catch (e: Exception) {
            e.printStackTrace()
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
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
                val action =
                    HomeFragmentDirections.actionHomeFragmentToGameFragment(
                        matchType
                    )
                navController.navigate(action)
            }
            R.id.gameFragment -> {
                val action =
                    GameFragmentDirections.actionGameFragmentToGameFragment(
                        matchType
                    )
                val navOptions =
                    NavOptions.Builder().setLaunchSingleTop(true).build()
                navController.navigate(action, navOptions)
            }
            R.id.game2Fragment -> {
                val action =
                    Game2FragmentDirections.actionGame2FragmentToGameFragment(
                        matchType
                    )
                val navOptions =
                    NavOptions.Builder().setLaunchSingleTop(true).build()
                navController.navigate(action, navOptions)
            }
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.enter_from_right, 0)
            .replace(R.id.odds_detail_container, fragment)
            .addToBackStack(null)
            .commit()
    }


    override fun onBackPressed() {
        super.onBackPressed()

        tabLayout.getTabAt(0)?.select()
    }

    private fun initObserve() {
        viewModel.token.observe(this) {
            //登入成功後要做的事
            queryData()
            updateAvatar()
            updateMenuFragmentUI()
        }

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

            switchFragment(
                OddsDetailFragment.newInstance(
                    gameType, typeName, matchId, oddsType
                )
            )
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

    private fun updateUiWithResult(sportMenuResult: SportMenuResult) {
        if (sportMenuResult.success) {
            refreshTabLayout(sportMenuResult)
        }
    }

    private fun updateAvatar() {
        Glide.with(this)
            .load(sLoginData?.iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.ic_head))
            .into(iv_head) //載入頭像
    }

    private fun updateMenuFragmentUI() {
        try {
            val menuFrag = supportFragmentManager.findFragmentById(R.id.fragment_menu) as MenuFragment
            menuFrag.updateUI()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

}
