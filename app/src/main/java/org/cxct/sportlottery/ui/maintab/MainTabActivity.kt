package org.cxct.sportlottery.ui.maintab

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.activity_main_tab.*
import kotlinx.android.synthetic.main.bet_bar_layout.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavActivity
import org.cxct.sportlottery.ui.game.betList.BetListFragment
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.accountHistory.next.AccountHistoryNextFragment
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.maintab.menu.MainLeftFragment
import org.cxct.sportlottery.ui.maintab.menu.SportLeftFragment
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterFragment
import org.cxct.sportlottery.ui.sport.favorite.FavoriteFragment
import org.cxct.sportlottery.util.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainTabActivity : BaseBottomNavActivity<MainTabViewModel>(MainTabViewModel::class) {

    lateinit var fragmentHelper: FragmentHelper
    var fragments = arrayOf<Fragment>(
        HomeFragment.newInstance(),
        SportFragment.newInstance(),
        BetRecordFragment.newInstance(),
        FavoriteFragment.newInstance(),
        ProfileCenterFragment.newInstance()
    )
    private var betListFragment = BetListFragment()
    private var homeLeftFragment = MainLeftFragment()
    private var sportLeftFragment = SportLeftFragment()
    companion object {
        fun reStart(context: Context) {
            val intent = Intent(context, MainTabActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        fun start2Tab(context: Context, startTabPosition: Int) {
            val intent = Intent(context, MainTabActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("startTabPosition", startTabPosition)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_tab)
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .fitsSystemWindows(false)
            .init()
        initDrawerLayout()
        initMenu()
        initBottomFragment()
        initBottomNavigation()
        initObserve()
    }

    override fun onNightModeChanged(mode: Int) {
        super.onNightModeChanged(mode)
        reStart(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.getIntExtra("startTabPosition", 0)?.let {
            bottom_navigation_view.currentItem = it
        }
    }

    private fun initObserve() {
        viewModel.userMoney.observe(this) {
            it?.let { money ->
                cl_bet_list_bar.tv_balance.text = TextUtil.formatMoney(money)
            }
        }
        viewModel.showBetInfoSingle.observe(this) {
            it.getContentIfNotHandled()?.let {
                showBetListPage()
            }
        }
        viewModel.showBetUpperLimit.observe(this) {
            if (it.getContentIfNotHandled() == true)
                snackBarBetUpperLimitNotify.apply {
                    setAnchorView(R.id.cl_bet_list_bar)
                    show()
                }
        }
    }

    private fun initBottomFragment() {
        fragmentHelper = FragmentHelper(supportFragmentManager, R.id.fl_content, fragments)
        bottom_navigation_view.apply {
            enableAnimation(false)
            enableShiftingMode(false)
            setTextVisibility(true)
            setTextSize(10f)
            setIconSize(30f)
            onNavigationItemSelectedListener =
                BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.i_betlist, R.id.i_favorite, R.id.i_user -> {
                            if (viewModel.isLogin.value == false) {
                                startActivity(Intent(this@MainTabActivity,
                                    LoginActivity::class.java))
                                return@OnNavigationItemSelectedListener false
                            }
                        }
                    }
                    if (this.getMenuItemPosition(menuItem)!=0){
                        ll_home_back.visibility = View.GONE
                    }
                    fragmentHelper.showFragment(this.getMenuItemPosition(menuItem))
                    return@OnNavigationItemSelectedListener true
                }
        }
        intent?.getIntExtra("startTabPosition", 0)?.let {
            bottom_navigation_view.currentItem = it
        }
    }

    open fun openDrawerLayout() {
        drawerLayout.openDrawer(Gravity.LEFT)
    }

    private fun initDrawerLayout() {
        showLeftFrament(0)
//        drawerLayout.setScrimColor(Color.TRANSPARENT)
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
//                val mContent: View = drawerLayout.getChildAt(0)
//                //设置1.1，让主界面更缩小
//                val scale = 1 - slideOffset
//                val rightScale = 0.55f + scale * 0.45f
//                if (drawerView.tag == "LEFT") {
//                    val leftScale = 1 - scale
//                    drawerView.scaleX = leftScale
//                    drawerView.scaleY = leftScale
//                    drawerView.alpha = 1f
//                    mContent.translationX = drawerView.measuredWidth * (1 - scale) * 0.94f
//                    mContent.pivotX = 0f
//                    mContent.pivotY = (mContent.measuredHeight / 2).toFloat()
//                    mContent.invalidate()
//                    mContent.scaleX = rightScale
//                    mContent.scaleY = rightScale
//                }
            }

            override fun onDrawerOpened(drawerView: View) {
                if (drawerView.tag == "LEFT") {
                    drawerLayout.setDrawerLockMode(
                        DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                        Gravity.RIGHT
                    )
                } else {
                    drawerLayout.setDrawerLockMode(
                        DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                        Gravity.LEFT
                    )

                }
            }

            override fun onDrawerClosed(drawerView: View) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        })
    }

    fun showLeftFrament(position: Int, fromPage: Int = -1) {
      //  LogUtil.d("position=" + position + ",fromPage=" + fromPage)
        when (position) {
            0 -> {
                homeLeftFragment.fromPage = fromPage
                supportFragmentManager.beginTransaction()
                    .replace(R.id.left_menu, homeLeftFragment)
                    .commit()
            }
            else -> {
                sportLeftFragment.currentTab = fromPage
                supportFragmentManager.beginTransaction()
                    .replace(R.id.left_menu, sportLeftFragment)
                    .commit()
            }
        }
    }

    override fun initMenu() {
        try {
            //關閉側邊欄滑動行為
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            //選單選擇結束要收起選單
            left_menu.layoutParams.width = (MetricsUtil.getScreenWidth() * 0.75).toInt() //動態調整側邊欄寬

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMenuEvent(event: MenuEvent) {
        if (event.open) {
            drawerLayout.openDrawer(Gravity.LEFT)
        } else {
            drawerLayout.closeDrawer(Gravity.LEFT)
        }
    }

    override fun onBackPressed() {
        //非注單詳情頁，重新顯示BottomNavBar
        val fragment = supportFragmentManager.findFragmentByTag(AccountHistoryNextFragment::class.java.simpleName)
        if (fragment == null) setupBottomNavBarVisibility(true)

        //返回鍵優先關閉投注單fragment
        if (supportFragmentManager.backStackEntryCount != 0) {
            for (i in 0 until supportFragmentManager.backStackEntryCount) {
                supportFragmentManager.popBackStack()
            }
            return
        }
        super.onBackPressed()
    }

    override fun getBetListPageVisible(): Boolean {
        return betListFragment.isVisible
    }

    var betListCount = 0
    override fun updateBetListCount(num: Int) {
        betListCount = num
        cl_bet_list_bar.isVisible = num > 0
        cl_bet_list_bar.tv_bet_list_count.text = num.toString()
        if (num > 0) viewModel.getMoney()
    }

    override fun showLoginNotify() {
        snackBarLoginNotify.apply {
            setAnchorView(R.id.bottom_navigation_view)
            show()
        }
    }

    override fun showMyFavoriteNotify(myFavoriteNotifyType: Int) {
        setSnackBarMyFavoriteNotify(myFavoriteNotifyType)
        snackBarMyFavoriteNotify?.apply {
            setAnchorView(R.id.bottom_navigation_view)
            show()
        }
    }

    override fun updateUiWithLogin(isLogin: Boolean) {
        if (isLogin) {


        } else {


        }
    }

    override fun updateOddsType(oddsType: OddsType) {
        //  tv_odds_type.text = getString(oddsType.res)
    }

    override fun navOneSportPage(thirdGameCategory: ThirdGameCategory?) {
        if (thirdGameCategory != null) {
            val intent = Intent(this, MainActivity::class.java)
                .putExtra(MainActivity.ARGS_THIRD_GAME_CATE, thirdGameCategory)
            startActivity(intent)

            return
        }

        startActivity(Intent(this, GamePublicityActivity::class.java))
    }

    override fun initToolBar() {
    }

    override fun clickMenuEvent() {
    }

    override fun initBottomNavigation() {
        cl_bet_list_bar.tv_balance_currency.text = sConfigData?.systemCurrencySign
        cl_bet_list_bar.tv_balance.text = TextUtil.formatMoney(0.0)
        cl_bet_list_bar.setOnClickListener {
            showBetListPage()
        }
    }

    override fun showBetListPage() {
        betListFragment =
            BetListFragment.newInstance(object : BetListFragment.BetResultListener {
                override fun onBetResult(
                    betResultData: Receipt?,
                    betParlayList: List<ParlayOdd>,
                    isMultiBet: Boolean,
                ) {
                    showBetReceiptDialog(betResultData, betParlayList, isMultiBet, R.id.fl_bet_list)
                }

            })

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit,
                R.anim.push_bottom_to_top_enter,
                R.anim.pop_bottom_to_top_exit
            )
            .add(R.id.fl_bet_list, betListFragment)
            .addToBackStack(BetListFragment::class.java.simpleName)
            .commit()
    }

    fun setupBetData(fastBetDataBean: FastBetDataBean) {
        viewModel.updateMatchBetListData(fastBetDataBean)
    }

    fun setupBottomNavBarVisibility(isVisible: Boolean) {
        bottom_navigation_view.isVisible = isVisible
        if (isVisible) {
            cl_bet_list_bar.isVisible = betListCount > 0
        } else {
            cl_bet_list_bar.isVisible = false
        }
    }

    fun goBetRecordDetails(date: String, gameType: String) {
        setupBottomNavBarVisibility(false)
        supportFragmentManager.beginTransaction()
            .add(R.id.fl_content, AccountHistoryNextFragment.newInstance(date, gameType))
            .addToBackStack(AccountHistoryNextFragment::class.java.simpleName)
            .commit()
    }

    fun switchTabByPosition(position: Int) {
        bottom_navigation_view.currentItem = position
    }

    fun jumpToTheSport(matchType: MatchType, gameType: GameType) {
        (fragments[1] as SportFragment).setJumpSport(matchType, gameType)
        bottom_navigation_view.currentItem = 1
    }

    fun jumpToHome(tabPosition: Int) {
        (fragments[0] as HomeFragment).switchTabByPosition(tabPosition)
        bottom_navigation_view.currentItem = 0
    }

    fun jumpToBetInfo(tabPosition: Int) {
        (fragments[2] as BetRecordFragment).selectTab(tabPosition)
        bottom_navigation_view.currentItem = 2
    }

    fun homeBackView(boolean: Boolean) {
        if (boolean) {
            ll_home_back.visibility = View.VISIBLE
            bottom_navigation_view.getBottomNavigationItemView(0).visibility = View.INVISIBLE
        } else {
            bottom_navigation_view.getBottomNavigationItemView(0).visibility = View.VISIBLE
            ll_home_back.visibility = View.GONE
        }
        ll_home_back.setOnClickListener {
            (fragments[0] as HomeFragment).switchTabByPosition(0)
        }

    }
}
