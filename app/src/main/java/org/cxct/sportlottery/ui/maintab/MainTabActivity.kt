package org.cxct.sportlottery.ui.maintab

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.gyf.immersionbar.ImmersionBar
import com.luck.picture.lib.tools.ToastUtils
import kotlinx.android.synthetic.main.activity_main_tab.*
import kotlinx.android.synthetic.main.bet_bar_layout.view.*
import kotlinx.android.synthetic.main.content_bet_info_item_v3.view.tvOdds
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.HomeTabEvent
import org.cxct.sportlottery.event.MainTabEvent
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.extentions.gone
import org.cxct.sportlottery.extentions.visible
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavActivity
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
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
import org.cxct.sportlottery.ui.sport.SportLeagueAdapter
import org.cxct.sportlottery.ui.sport.favorite.FavoriteFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.system.exitProcess


class MainTabActivity : BaseBottomNavActivity<MainTabViewModel>(MainTabViewModel::class) {

    val fragmentHelper: FragmentHelper by lazy {
        FragmentHelper(
            supportFragmentManager, R.id.fl_content, arrayOf(
                HomeFragment::class.java,
                SportFragment::class.java,
                BetRecordFragment::class.java,
                FavoriteFragment::class.java,
                ProfileCenterFragment::class.java
            )
        )
    }

    val norTabIcons by lazy {
        arrayOf(
            R.drawable.selector_tab_home,
            R.drawable.selector_tab_sport,
            R.drawable.selector_tab_betlist,
            R.drawable.selector_tab_fav,
            R.drawable.selector_tab_user
        )
    }

    val cupTabIcons by lazy {
        arrayOf(
            R.drawable.selector_tab_home_cup,
            R.drawable.selector_tab_sport_cup,
            R.drawable.selector_tab_betlist_cup,
            R.drawable.selector_tab_fav_cup,
            R.drawable.selector_tab_user_cup
        )
    }

    private var betListFragment: BetListFragment? = null
    private val homeLeftFragment by lazy { MainLeftFragment() }
    private val sportLeftFragment by lazy { SportLeftFragment() }
    private var exitTime: Long = 0

    companion object {

        var activityInstance: MainTabActivity? = null

        fun reStart(context: Context) {
            val intent = Intent(context, MainTabActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        fun start2Tab(context: Context, position: Int) {
            if (activityInstance != null) {
                activityInstance!!.switchTabByPosition(position)
            } else {
                val intent = Intent(context, MainTabActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("startTabPosition", position)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        SportLeagueAdapter.clearCachePool()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_tab)
        ImmersionBar.with(this).statusBarDarkFont(true).transparentStatusBar()
            .fitsSystemWindows(false).init()
        initDrawerLayout()
        initMenu()
        initBottomFragment()
        initBottomNavigation()
        initObserve()
        activityInstance = this
        EventBusUtil.targetLifecycle(this)
    }

    var isWorldcupModel = false

    @SuppressLint("RestrictedApi")
    private fun resetBottomTheme(worldcupModel: Boolean) {
        isWorldcupModel = worldcupModel
        var textColor: ColorStateList
        val iconArray = if (worldcupModel) {
            iv_home_back.setImageResource(R.drawable.icon01_arrow_back_sel)
            tv_home_back.setTextColor(resources.getColor(R.color.color_CC0054))
            bottom_navigation_view.setBackgroundResource(R.color.color_B2_FFFFFF)
            ((bottom_navigation_view.parent as View).layoutParams as MarginLayoutParams).topMargin =
                0
            textColor = resources.getColorStateList(R.color.main_tab_cup_text_selector)
            cupTabIcons
        } else {
            iv_home_back.setImageResource(R.drawable.icon01_arrow_back)
            tv_home_back.setTextColor(resources.getColor(R.color.color_025BE8))
            bottom_navigation_view.setBackgroundResource(R.drawable.bg_icon_bottom_bar)
            ((bottom_navigation_view.parent as View).layoutParams as MarginLayoutParams).topMargin =
                -8.dp
            textColor = resources.getColorStateList(R.color.main_tab_text_selector)
            norTabIcons
        }


        repeat(bottom_navigation_view.itemCount) {
            bottom_navigation_view.getBottomNavigationItemView(it).run {
                setIcon(resources.getDrawable(iconArray[it]))
                setTextColor(textColor)
            }
        }
    }

    @Subscribe
    fun onHomeTab(event: HomeTabEvent) {
        resetBottomTheme(event.isWorldCupTab())
    }

    override fun onNightModeChanged(mode: Int) {
        super.onNightModeChanged(mode)
        reStart(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        activityInstance = this
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
            if (it.getContentIfNotHandled() == true) snackBarBetUpperLimitNotify.apply {
                setAnchorView(R.id.cl_bet_list_bar)
                show()
            }
        }
    }

    private fun initBottomFragment() {
        ll_home_back.setOnClickListener {
            (fragmentHelper.getFragment(0) as HomeFragment).switchTabByPosition(0)
        }
        bottom_navigation_view.apply {
            enableAnimation(false)
            enableShiftingMode(false)
            setTextVisibility(true)
            setTextSize(10f)
            setIconSize(30f)

            onNavigationItemSelectedListener =
                BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
                    var wordcupModel = false
                    when (menuItem.itemId) {
                        R.id.i_betlist, R.id.i_favorite, R.id.i_user -> {
                            if (viewModel.isLogin.value == false) {
                                startActivity(
                                    Intent(
                                        this@MainTabActivity, LoginActivity::class.java
                                    )
                                )
                                return@OnNavigationItemSelectedListener false
                            }
                        }

                        R.id.i_home -> {
                            wordcupModel = isWorldcupModel
                        }
                    }

                    resetBottomTheme(wordcupModel)

                    val position = getMenuItemPosition(menuItem)

                    val fragment = fragmentHelper.showFragment(position)
                    if (position == 0) {
                        (fragmentHelper.getFragment(0) as HomeFragment).switchTabByPosition(0)
                    } else {
                        ll_home_back.visibility = View.GONE
                    }

                    setupBetBarVisiblity(position)
                    EventBusUtil.post(MainTabEvent(fragment))
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
                        DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT
                    )
                } else {
                    drawerLayout.setDrawerLockMode(
                        DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.LEFT
                    )

                }
            }

            override fun onDrawerClosed(drawerView: View) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        })
    }

    fun showLeftFrament(position: Int, fromPage: Int = -1) {
        if (position == 0) {
            supportFragmentManager.beginTransaction().replace(R.id.left_menu, homeLeftFragment)
                .commit()
            homeLeftFragment.fromPage = fromPage
            return
        }

        supportFragmentManager.beginTransaction().replace(R.id.left_menu, sportLeftFragment)
            .commit()

        val currentFragment = fragmentHelper.getCurrentFragment()
        if (currentFragment is SportFragment) {
            sportLeftFragment.matchType = currentFragment.getCurMatchType()
            sportLeftFragment.gameType = currentFragment.getCurGameType()
            return
        }

        if (currentFragment is HomeFragment) {
            if (currentFragment.isWorldCupTab()) {
                sportLeftFragment.selectWorldCup()
            }
        }


    }

    override fun initMenu() {
        try {
            //關閉側邊欄滑動行為
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            drawerLayout.setScrimColor(getColor(R.color.transparent_black_20))
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


    //系统方法
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!showBottomNavBar()) {
                return false
            }
            exit()
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    fun showBottomNavBar(): Boolean {
        //非注單詳情頁，重新顯示BottomNavBar
        val fragment =
            supportFragmentManager.findFragmentByTag(AccountHistoryNextFragment::class.java.simpleName)
        if (fragment == null) setupBottomNavBarVisibility(true)

        //返回鍵優先關閉投注單fragment
        if (supportFragmentManager.backStackEntryCount != 0) {
            for (i in 0 until supportFragmentManager.backStackEntryCount) {
                supportFragmentManager.popBackStack()
            }
            return false
        }
        return true
    }

    private fun exit() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            ToastUtils.s(this, getString(R.string.str_press_again_to_exit_the_program))
            exitTime = System.currentTimeMillis()
        } else {
            finish()
            exitProcess(0)
        }
    }


    override fun getBetListPageVisible(): Boolean {
        return betListFragment?.isVisible ?: false
    }

    var betListCount = 0
    override fun updateBetListCount(num: Int) {
        betListCount = num
        setupBetBarVisiblity(bottom_navigation_view.currentItem)
        cl_bet_list_bar.tv_bet_list_count.text = num.toString()
        if (num > 0) viewModel.getMoney()
    }

    /**
     * 单关不显示赔率
     * 串关显示赔率
     */
    override fun updateBetListOdds(list: MutableList<BetInfoListData>) {
        if (list.size > 1) {
            val multipleOdds = getMultipleOdds(list)
            cl_bet_list_bar.tvOdds.text = multipleOdds
            cl_bet_list_bar.tvOdds.visible()
        } else {
            cl_bet_list_bar.tvOdds.gone()
        }

    }

    fun setupBetBarVisiblity(position: Int) {
        var needShowBetBar = when (position) {
            0, 1, 3 -> true
            else -> false
        }
        cl_bet_list_bar.isVisible = needShowBetBar && betListCount > 0
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
            val intent = Intent(this, MainActivity::class.java).putExtra(
                MainActivity.ARGS_THIRD_GAME_CATE, thirdGameCategory
            )
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
        val ft = supportFragmentManager.beginTransaction()
        betListFragment?.let {
            if (it.isAdded) {
                ft.remove(it)
            }
        }

        betListFragment = BetListFragment.newInstance(object : BetListFragment.BetResultListener {
            override fun onBetResult(
                betResultData: Receipt?, betParlayList: List<ParlayOdd>, isMultiBet: Boolean
            ) {
                showBetReceiptDialog(betResultData, betParlayList, isMultiBet, R.id.fl_bet_list)
            }
        })


        ft
//            .setCustomAnimations(
//            R.anim.pickerview_slide_in_bottom,
//            R.anim.pickerview_slide_out_bottom,
//            R.anim.pickerview_slide_in_bottom,
//            R.anim.pickerview_slide_out_bottom
//        )
            .add(R.id.fl_bet_list, betListFragment!!)
            .addToBackStack(null).commit()
    }

//    private fun getInAnimation(): Animation? {
//        val res = PickerViewAnimateUtil.getAnimationResource(this.animGravity, true)
//        return AnimationUtils.loadAnimation(context, res)
//    }
//
//    private fun getOutAnimation(): Animation? {
//        val res = PickerViewAnimateUtil.getAnimationResource(this.animGravity, false)
//        return AnimationUtils.loadAnimation(context, res)
//    }

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
            .addToBackStack(AccountHistoryNextFragment::class.java.simpleName).commit()
    }

    fun switchTabByPosition(position: Int) {
        if (bottom_navigation_view.currentItem != position) {
            bottom_navigation_view.currentItem = position
        }
    }

    fun jumpToTheSport(matchType: MatchType, gameType: GameType) {
        if (bottom_navigation_view.currentItem != 1) {
            bottom_navigation_view.currentItem = 1
        }
        (fragmentHelper.getFragment(1) as SportFragment).setJumpSport(matchType, gameType)
    }

    fun jumpToHome(tabPosition: Int) {
        if (bottom_navigation_view.currentItem != 0) {
            bottom_navigation_view.currentItem = 0
        }
        (fragmentHelper.getFragment(0) as HomeFragment).switchTabByPosition(tabPosition)
    }

    fun jumpToBetInfo(tabPosition: Int) {
        if (bottom_navigation_view.currentItem != 2) {
            bottom_navigation_view.currentItem = 2
        }
        (fragmentHelper.getFragment(2) as BetRecordFragment).selectTab(tabPosition)
    }

    fun homeBackView(boolean: Boolean) {
        if (boolean) {
            ll_home_back.visibility = View.VISIBLE
            bottom_navigation_view.getBottomNavigationItemView(0).visibility = View.INVISIBLE
        } else {
            bottom_navigation_view.getBottomNavigationItemView(0).visibility = View.VISIBLE
            ll_home_back.visibility = View.GONE
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (activityInstance == this) {
            activityInstance == null
        }
        SportLeagueAdapter.clearCachePool()
    }
}
