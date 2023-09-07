package org.cxct.sportlottery.ui.maintab

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.gyf.immersionbar.ImmersionBar
import com.luck.picture.lib.tools.ToastUtils
import kotlinx.android.synthetic.main.activity_main_tab.*
import kotlinx.android.synthetic.main.custom_bottom_sheet_item.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.event.BetModeChangeEvent
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.event.NetWorkEvent
import org.cxct.sportlottery.common.event.SportStatusEvent
import org.cxct.sportlottery.common.event.ShowFavEvent
import org.cxct.sportlottery.common.event.ShowInPlayEvent
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ActivityMainTabBinding
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.ConfigRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.ui.base.BaseBottomNavActivity
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.betList.BetListFragment
import org.cxct.sportlottery.ui.betList.adapter.BetListRefactorAdapter
import org.cxct.sportlottery.ui.betRecord.BetRecordActivity
import org.cxct.sportlottery.ui.chat.ChatActivity
import org.cxct.sportlottery.ui.maintab.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.maintab.games.OKGamesFragment
import org.cxct.sportlottery.ui.maintab.games.OKLiveFragment
import org.cxct.sportlottery.ui.maintab.home.HomeFragment
import org.cxct.sportlottery.ui.maintab.menu.MainLeftFragment2
import org.cxct.sportlottery.ui.maintab.menu.SportLeftMenuFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterFragment
import org.cxct.sportlottery.ui.sport.SportFragment2
import org.cxct.sportlottery.ui.sport.oddsbtn.OddsButton2
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.dialog.PopImageDialog
import org.cxct.sportlottery.view.dialog.ToGcashDialog
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import splitties.activities.start
import kotlin.reflect.jvm.internal.impl.types.checker.TypeRefinementSupport.Enabled
import kotlin.system.exitProcess


class MainTabActivity : BaseBottomNavActivity<MainTabViewModel>(MainTabViewModel::class) {

    private val fragmentHelper: FragmentHelper by lazy {
        FragmentHelper(
            supportFragmentManager, R.id.fl_content, arrayOf(
                Param(HomeFragment::class.java),
                Param(SportFragment2::class.java),
                Param(OKGamesFragment::class.java),
                Param(OKGamesFragment::class.java), // 占坑
                Param(ProfileCenterFragment::class.java),
            )
        )
    }

    private var betListFragment: BetListFragment? = null

    private var exitTime: Long = 0

    companion object {

        var activityInstance: MainTabActivity? = null

        /**
         * fromLoginOrReg  是否来自登录或者注册页面
         */
        fun reStart(context: Context, showDialog: Boolean = false,fromLoginOrReg: Boolean = false) {
            if (fromLoginOrReg){
                ToGcashDialog.needShow = true
            }
            PopImageDialog.showHomeDialog = showDialog
            PopImageDialog.showSportDialog = showDialog
            PopImageDialog.showOKGameDialog = showDialog
            PopImageDialog.showOKLiveDialog = showDialog
            val intent = Intent(context, MainTabActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    private val binding by lazy { ActivityMainTabBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        ImmersionBar.with(this).statusBarDarkFont(true).transparentStatusBar()
            .fitsSystemWindows(false).init()
        initDrawerLayout()
        initMenu()
        initBottomFragment(savedInstanceState?.getInt("startTabPosition") ?: 0)
        initBottomNavigation()
        initObserve()
        activityInstance = this
        EventBusUtil.targetLifecycle(this)
        LotteryManager.instance.getLotteryInfo()
        ConfigRepository.onNewConfig(this) {
            changeChatTabStatus(getString(R.string.N984), R.drawable.selector_tab_chat)
//            if (isOpenChatRoom()) {
//                changeChatTabStatus(getString(R.string.N984), R.drawable.selector_tab_chat)
//            } else {
//                changeChatTabStatus(getString(R.string.main_tab_favorite), R.drawable.selector_tab_fav)
//            }
        }
    }

    private fun changeChatTabStatus(title: String, @DrawableRes icon: Int) {
        val item = binding.bottomNavigationView.menu.findItem(R.id.i_favorite)
        if (item.title == title) {
            return
        }

        item.title = title
        item.icon = getDrawable(icon)
    }

    override fun onNightModeChanged(mode: Int) {
        super.onNightModeChanged(mode)
        reStart(this)
    }

    private fun initObserve() {

        //设置体育服务监听
        setupSportStatusChange(this) {
            //如果维护开启，当前在体育相关fragment， 退回到首页
            if (checkMainPosition(getCurrentPosition())) {
                //关闭已选中的投注
                closeBetFragment()
                //回到首页
                binding.bottomNavigationView.postDelayed({
                    backMainHome()
                }, 200)
            }

            EventBusUtil.post(SportStatusEvent(it))
        }
        viewModel.showBetInfoSingle.observe(this) {
            it.getContentIfNotHandled()?.let {
                showBetListPage()
            }
        }
        viewModel.showBetUpperLimit.observe(this) {
            if (it.getContentIfNotHandled() == true) {
                showSnackBarBetUpperLimitNotify(
                    getString(R.string.bet_notify_max_limit)
                ).setAnchorView(R.id.parlayFloatWindow).show()
            }
        }

        viewModel.showBetBasketballUpperLimit.observe(this) {
            if (it.getContentIfNotHandled() == true) {
                showSnackBarBetUpperLimitNotify(
                    getString(R.string.bet_basketball_notify_max_limit)
                ).setAnchorView(R.id.parlayFloatWindow).show()
            }
        }
    }


    /**
     * 关闭投注相关的购物车
     */
    private fun closeBetFragment() {
        //投注fragment如果已显示
        if (getBetListPageVisible()) {
            //关闭
            betListFragment?.onBackPressed()
        }
        //移除选中的投注信息
        betListFragment?.viewModel?.removeBetInfoAll()
        //隐藏购物车view
        parlayFloatWindow?.gone()
    }

    /**
     * 检查是否为体育相关的fragment
     */
    fun checkSportFragment(position: Int): Boolean {
        val fragment = fragmentHelper.getFragment(position)
        if (fragment is SportFragment2) {
            return true
        }

        return false
    }


    private fun initBottomFragment(position: Int) {
        binding.bottomNavigationView.apply {
            enableAnimation(false)
            enableShiftingMode(false)
            setTextVisibility(true)
            setTextSize(10f)
            setIconSize(24f)
            menu.getItem(2).isVisible = !getMarketSwitch()
            onNavigationItemSelectedListener =
                BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
                    if (mIsEnabled) {
                        avoidFastDoubleClick()
                        ImmersionBar.with(this@MainTabActivity)
                            .statusBarDarkFont(true)
                            .init()

                        val itemPosition = getMenuItemPosition(menuItem)
                        if (checkMainPosition(itemPosition)) {
                            return@OnNavigationItemSelectedListener false
                        }
                        if(itemPosition==1&&!StaticData.okSportOpened()){
                            ToastUtil.showToast(this@MainTabActivity,getString(R.string.N700))
                            return@OnNavigationItemSelectedListener  false
                        }
                        if(itemPosition==2&&!StaticData.okGameOpened()){
                            ToastUtil.showToast(this@MainTabActivity,getString(R.string.N700))
                            return@OnNavigationItemSelectedListener  false
                        }

                        when (menuItem.itemId) {
                            R.id.i_user -> {
                                if (viewModel.isLogin.value == false) {
                                    startLogin()
                                    return@OnNavigationItemSelectedListener false
                                }
                            }

                            R.id.i_favorite -> {
                                start<ChatActivity> {}
                                return@OnNavigationItemSelectedListener false
                            }
                        }

                        fragmentHelper.showFragment(itemPosition)
                        if (itemPosition == 0) {
                            enableSelectBottomNav(true)
                        }
                        homeFragment().backMainHome()
                        setupBetBarVisiblity(itemPosition)
                        return@OnNavigationItemSelectedListener true
                    }
                    return@OnNavigationItemSelectedListener false
                }


        }

        // 如果回复之前的position会有很多其它崩溃异常
        binding.bottomNavigationView.currentItem = 0 /*position*/
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("startTabPosition", bottom_navigation_view.currentItem)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(Bundle()) // 如果回复之前的position会有很多其它崩溃异常
    }

    open fun openDrawerLayout() {
        drawerLayout.openDrawer(Gravity.LEFT)
    }

    fun closeDrawerLayout() {
        drawerLayout.closeDrawer(Gravity.LEFT)
    }

    private fun initDrawerLayout() {
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


    fun showMainLeftMenu(contentFragment: Class<BaseFragment<*>>?) {
        fragmentHelper2.show(MainLeftFragment2::class.java, Bundle()) { fragment, _ ->
            fragment.openWithFragment(contentFragment)
        }

    }

    private val fragmentHelper2 by lazy { FragmentHelper2(supportFragmentManager, R.id.left_menu) }
    fun showSportLeftMenu() {
        fragmentHelper2.show(SportLeftMenuFragment::class.java, Bundle()) { fragment, instance ->
            if(!instance){
                fragment.reloadData()
            }
        }

//        supportFragmentManager.beginTransaction().replace(R.id.left_menu, sportLeftFragment)
//            .commit()

//        sportLeftFragment.matchType = matchType
//        sportLeftFragment.gameType = gameType
    }


    override fun initMenu() {
        try {
            //關閉側邊欄滑動行為
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            drawerLayout.setScrimColor(getColor(R.color.transparent_black_20))
//            //選單選擇結束要收起選單
            left_menu.layoutParams.width = MetricsUtil.getScreenWidth() //動態調整側邊欄寬

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNetValidEvent(event: NetWorkEvent) {
        //网络恢复
        if (event.isValid) {
            val fragment = fragmentHelper.getFragment(0)
            if (fragment is HomeFragment) {
                //更新config   刷新体育服务开关
                fragment.viewModel.getConfigData()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onShowInPlay(event: ShowInPlayEvent) {
        binding.bottomNavigationView.postDelayed({
            jumpToTheSport(MatchType.IN_PLAY, GameType.BK)
        },200)

    }


    @Subscribe
    fun onShowFavEvent(event: ShowFavEvent) {
        showLoginNotify()
    }

    @Subscribe
    fun onBetModeChangeEvent(event: BetModeChangeEvent) {
        if (event.currentMode == BetListFragment.SINGLE) {
            BetInfoRepository.currentBetType = BetListFragment.SINGLE
            parlayFloatWindow.gone()
        } else if (event.currentMode == BetListFragment.BASKETBALL_ENDING_CARD) {
            BetInfoRepository.currentBetType = BetListFragment.BASKETBALL_ENDING_CARD
            if (betListCount != 0) {
                parlayFloatWindow.visible()
            }
        } else {
            BetInfoRepository.currentBetType = BetListFragment.PARLAY
            if (betListCount != 0) {
                parlayFloatWindow.visible()
            }
        }
    }


    //系统方法
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (drawerLayout?.isOpen == true) {
                drawerLayout?.close()
                return false
            }
            if (!showBottomNavBar()) {
                return false
            }
            exit()
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun showBottomNavBar(): Boolean {
        setupBottomNavBarVisibility(true)

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

    private var betListCount = 0

    override fun updateBetListCount(num: Int) {
        betListCount = num
        setupBetBarVisiblity(bottom_navigation_view.currentItem)
        parlayFloatWindow.updateCount(betListCount.toString())
        if (num > 0) viewModel.getMoneyAndTransferOut()
    }


    private fun setupBetBarVisiblity(position: Int) {
        val needShowBetBar = when (position) {
            0, 1, 3 -> true
            else -> false
        }

        if (betListCount == 0 || !needShowBetBar || BetInfoRepository.currentBetType
            == BetListFragment.SINGLE
        ) {
//            Timber.d("ParlayFloatWindow隐藏：betListCount:${betListCount} !needShowBetBar:${!needShowBetBar} currentBetMode:${BetInfoRepository.currentBetType}")
            parlayFloatWindow.gone()
        } else {
            if (BetInfoRepository.currentBetType == BetListFragment.PARLAY
            ) {
                parlayFloatWindow.setBetText(getString(R.string.conspire))
                parlayFloatWindow.updateCount(betListCount.toString())
            } else {
                parlayFloatWindow.setBetText(getString(R.string.bet_slip))
            }
            parlayFloatWindow.visible()
        }
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
//        if (thirdGameCategory != null) {
//            val intent = Intent(this, MainActivity::class.java).putExtra(
//                MainActivity.ARGS_THIRD_GAME_CATE, thirdGameCategory
//            )
//            startActivity(intent)
//
//            return
//        }
//
//        startActivity(Intent(this, GamePublicityActivity::class.java))
    }

    override fun initToolBar() {
    }

    override fun clickMenuEvent() {
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun initBottomNavigation() {
//        parlayFloatWindow.tv_balance_currency.text = sConfigData?.systemCurrencySign
//        parlayFloatWindow.tv_balance.text = TextUtil.formatMoney(0.0)
        binding.parlayFloatWindow.onViewClick = {
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
            .add(R.id.fl_bet_list, betListFragment!!).addToBackStack(null).commit()
    }


    fun setupBetData(fastBetDataBean: FastBetDataBean, view: View? = null) {
        viewModel.updateMatchBetListData(fastBetDataBean)
        if (view != null) {
//            addAction(view)
        }
    }

    private fun setupBottomNavBarVisibility(isVisible: Boolean) {
        bottom_navigation_view.isVisible = isVisible
        space1.isVisible = isVisible
        if (betListCount == 0) {
            parlayFloatWindow.gone()
        }

    }


    private inline fun homeFragment() = fragmentHelper.getFragment(0) as HomeFragment

    fun backMainHome() {
        enableSelectBottomNav(true)
        homeFragment().backMainHome()
        navToPosition(0)
    }
    fun jumpToOKGames() {
        if (getMarketSwitch()) {
            return
        }
        navToPosition(2)
    }

    fun jumpToOkLive(){
        if(StaticData.okLiveOpened()){
            backMainHome()
            homeFragment().jumpToOKLive()
            enableSelectBottomNav(false)
        }else{
            ToastUtil.showToast(this,getString(R.string.N700))
        }

    }

    private fun navToPosition(position: Int) {
        if (bottom_navigation_view.currentItem != position) {
            bottom_navigation_view.currentItem = position
        }
    }

    /**
     * 清除选中状态，由于组件必须选中一个，就默认选中第一个，并且设置未选中的样式
     */
    private fun enableSelectBottomNav(enable: Boolean) {
        if (enable){
            bottom_navigation_view.itemTextColor = ContextCompat.getColorStateList(this@MainTabActivity,R.color.main_tab_text_selector)
            bottom_navigation_view.menu[0].icon = ContextCompat.getDrawable(this@MainTabActivity,R.drawable.selector_tab_home)
        }else{
            bottom_navigation_view.itemTextColor = ContextCompat.getColorStateList(this@MainTabActivity,R.color.color_6C7BA8)
            bottom_navigation_view.menu[0].icon = ContextCompat.getDrawable(this@MainTabActivity,R.drawable.ic_tab_home_nor)
        }
    }

    fun jumpToESport() {
        checkSportStatus(this) {
            (fragmentHelper.getFragment(1) as SportFragment2).setJumpESport()
            navToPosition(1)
        }
    }

    fun jumpToSport(gameType: GameType) {
        checkSportStatus(this) {
            (fragmentHelper.getFragment(1) as SportFragment2).jumpToSport(gameType)
            navToPosition(1)
        }
    }

    fun jumpToNews() {
        backMainHome()
        homeFragment().jumpToNews()
    }
    fun jumpToTheSport(matchType: MatchType? = null, gameType: GameType? = null) {
        (fragmentHelper.getFragment(1) as SportFragment2).setJumpSport(matchType, gameType)
        navToPosition(1)
    }

    fun jumpToWorldCup() {
        navToPosition(0)
        enableSelectBottomNav(false)
        homeFragment().jumpToWorldCup()
    }
    fun jumpToWorldCupGame() {
        navToPosition(0)
        enableSelectBottomNav(false)
        homeFragment().jumpToWorldCupGame()
    }

    fun jumpToInplaySport() {
        //检测体育服务是否关闭
        checkSportStatus(this) {
            jumpToTheSport(MatchType.IN_PLAY, GameType.BK)
        }
    }

    fun jumpToEarlySport() {
        jumpToTheSport(MatchType.EARLY, GameType.FT)
    }
    fun jumpToBetInfo(tabPosition: Int) {
        if (getMarketSwitch()) {
            return
        }
        if(LoginRepository.isLogined()){
            startActivity(BetRecordActivity::class.java)
        }else{
            startLogin()
        }
//        if (bottom_navigation_view.currentItem != 2) {
//            bottom_navigation_view.currentItem = 2
//        }
//        (fragmentHelper.getFragment(2) as BetRecordFragment).selectTab(tabPosition)
    }

    override fun onDestroy() {
        super.onDestroy()
        OddsButton2.clearOddsViewCaches()
        if (activityInstance == this) {
            activityInstance = null
        }
    }


    open fun getCurrentPosition(): Int = fragmentHelper.getCurrentPosition()

}
