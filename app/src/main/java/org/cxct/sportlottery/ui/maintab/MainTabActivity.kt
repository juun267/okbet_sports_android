package org.cxct.sportlottery.ui.maintab

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import androidx.core.view.postDelayed
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.SimpleDrawerListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.gyf.immersionbar.ImmersionBar
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.appevent.SensorsEventUtil
import org.cxct.sportlottery.common.enums.GameEntryType
import org.cxct.sportlottery.common.event.BetModeChangeEvent
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.event.NetWorkEvent
import org.cxct.sportlottery.common.event.SportStatusEvent
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityMainTabBinding
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesFirm
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.ConfigRepository
import org.cxct.sportlottery.repository.GamePlayNameRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.service.dispatcher.DataResourceChange
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.betList.BetListFragment
import org.cxct.sportlottery.ui.betRecord.BetRecordActivity
import org.cxct.sportlottery.ui.chat.ChatActivity
import org.cxct.sportlottery.ui.login.signIn.LoginKycVerifyActivity
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.maintab.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.maintab.games.OKGamesFragment
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.ui.maintab.games.OKLiveFragment
import org.cxct.sportlottery.ui.maintab.home.HomeFragment
import org.cxct.sportlottery.ui.maintab.home.PreLoader
import org.cxct.sportlottery.ui.maintab.home.news.NewsHomeFragment
import org.cxct.sportlottery.ui.maintab.menu.MainLeftFragment
import org.cxct.sportlottery.ui.maintab.menu.MainRightFragment
import org.cxct.sportlottery.ui.maintab.menu.SportLeftMenuFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterFragment
import org.cxct.sportlottery.ui.promotion.PromotionListActivity
import org.cxct.sportlottery.ui.sport.SportFragment
import org.cxct.sportlottery.ui.sport.esport.ESportFragment
import org.cxct.sportlottery.ui.sport.oddsbtn.OddsButton2
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.FragmentHelper
import org.cxct.sportlottery.util.FragmentHelper2
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.KvUtils
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.MetricsUtil
import org.cxct.sportlottery.util.Param
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.checkSportStatus
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable
import org.cxct.sportlottery.util.getMarketSwitch
import org.cxct.sportlottery.util.getSportEnterIsClose
import org.cxct.sportlottery.util.isThirdTransferOpen
import org.cxct.sportlottery.util.loginedRun
import org.cxct.sportlottery.util.setupSportStatusChange
import org.cxct.sportlottery.util.showBetReceiptDialog
import org.cxct.sportlottery.util.showDataSourceChangedDialog
import org.cxct.sportlottery.util.showFavoriteNotify
import org.cxct.sportlottery.util.showLoginSnackbar
import org.cxct.sportlottery.util.startLogin
import org.cxct.sportlottery.view.dialog.PopImageDialog
import org.cxct.sportlottery.view.dialog.ToGcashDialog
import org.cxct.sportlottery.view.dialog.ToMayaDialog
import org.cxct.sportlottery.view.dialog.TrialGameDialog
import org.cxct.sportlottery.view.floatingbtn.LotteryManager
import org.cxct.sportlottery.view.transform.TransformInDialog
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.androidx.viewmodel.ext.android.viewModel
import splitties.activities.start
import kotlin.system.exitProcess


class MainTabActivity : BaseSocketActivity<MainTabViewModel,ActivityMainTabBinding>(MainTabViewModel::class) {

    override fun pageName() = "主页"

    private val gamesViewModel by viewModel<OKGamesViewModel>()
    private val fragmentHelper: FragmentHelper by lazy {
        FragmentHelper(
            supportFragmentManager, R.id.fl_content, arrayOf(
                Param(HomeFragment::class.java),
                Param(SportFragment::class.java),
                Param(OKGamesFragment::class.java),
                Param(ProfileCenterFragment::class.java),
                Param(OKLiveFragment::class.java),
                Param(NewsHomeFragment::class.java),
                Param(ESportFragment::class.java, needRemove = true),
            )
        )
    }

    private val INDEX_HOME = 0
    private val INDEX_SPORT = 1
    private val INDEX_OKGAMES = 2
    private val INDEX_PROFILE = 3
    private val INDEX_OKLIVE = 4
    private val INDEX_NEWS = 5
    private val INDEX_ESPORT = 6

    private var betListFragment: BetListFragment? = null

    private var exitTime: Long = 0

    companion object {

        var activityInstance: MainTabActivity? = null

        /**
         * fromLoginOrReg  是否来自登录或者注册页面
         */
        fun reStart(context: Context,fromLoginOrReg: Boolean = false) {
            if (fromLoginOrReg){
                ToGcashDialog.needShow = true
                ToMayaDialog.needShow = true
                PopImageDialog.resetImageType()
            }
            val intent = Intent(context, MainTabActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    private lateinit var tabHelper: MainTabInflate

    override fun onInitView() {
        ImmersionBar.with(this)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .fitsSystemWindows(false).init()
        initDrawerLayout()
        initMenu()
        tabHelper = MainTabInflate(this, binding.linTab, ::onTabClick)
        backMainHome()
        initBottomNavigation()
        initObserve()
        activityInstance = this
        EventBusUtil.targetLifecycle(this)
        LotteryManager.instance.getLotteryInfo()
        viewModel.getSportMenuFilter()
        viewModel.getGameCollectNum()
        viewModel.getThirdGames()
        viewModel.getTaskDetail()
        PreLoader.startPreload()
        jumpGameAfterLogin()
        checkNotificationOpen()
    }

    private fun onTabClick(tabName: Int): Boolean {
        val result = when(tabName) {
            R.string.menu -> { // 菜单
                val currentFragment = fragmentHelper.getCurrentFragment()
                onMenuEvent(MenuEvent(true))
                if (currentFragment is SportFragment) {
                    showSportLeftMenu()
                } else if (currentFragment is HomeFragment) {
                    showMainLeftMenu(currentFragment.getCurrentFragment()?.javaClass as Class<BaseFragment<*,*>>? )
                } else {
                    showMainLeftMenu(currentFragment.javaClass as Class<BaseFragment<*,*>>?)
                }
                false
            }

            R.string.bottom_nav_home -> {
                navToPosition(INDEX_HOME)
                return true
            }

            R.string.main_tab_sport -> { // 体育
                if (!StaticData.okSportOpened()){
                    ToastUtil.showToast(this@MainTabActivity,getString(R.string.N700))
                    false
                }else if (checkSportMaintain(true)) {
                    false
                } else {
                    navToPosition(INDEX_SPORT)
                    true
                }
            }

            R.string.B23 -> { // OKGames
                if(!StaticData.okGameOpened()) {
                    ToastUtil.showToast(this@MainTabActivity,getString(R.string.N700))
                    false
                } else {
                    navToPosition(INDEX_OKGAMES)
                    true
                }
            }

            R.string.N984 -> { // 聊天室
                start<ChatActivity>()
                false
            }

            R.string.J748 -> { // 优惠活动
                PromotionListActivity.startFrom(this, "主页底部tab")
                false
            }

            R.string.main_tab_mine -> { // 我的
                navToPosition(INDEX_PROFILE)
                true
            }

            else -> false
        }

        if (result) {
            setupBetBarVisiblity()
        }
        return result
    }

    override fun onNightModeChanged(mode: Int) {
        super.onNightModeChanged(mode)
        reStart(this)
    }

    fun checkSportMaintain(isSport: Boolean = checkSportFragment()): Boolean {
        if (isSport && getSportEnterIsClose()) {
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                showPromptDialogNoCancel(message = getString(R.string.N969)) { }
            }
            return true
        }
        return false
    }

    private fun initObserve() {
        DataResourceChange.observe(this) { closeBetFragment() }
        ConfigRepository.onNewConfig(this) {
            if (GamePlayNameRepository.resourceList==null) {
                GamePlayNameRepository.getIndexResourceJson()
            }
        }

        //设置体育服务监听
        setupSportStatusChange(this) {
            //如果维护开启，当前在体育相关fragment， 退回到首页
            if (checkSportMaintain()) {
                //关闭已选中的投注
                closeBetFragment()
                //回到首页
                binding.root.postDelayed({ backMainHome() }, 200)
            }

            EventBusUtil.post(SportStatusEvent(it))
        }
        viewModel.betInfoList.observe(this) {
            updateBetListCount(it.peekContent().size)
        }
        viewModel.notifyLogin.observe(this) {
            showLoginSnackbar()
        }
        viewModel.notifyMyFavorite.observe(this) {
            it.getContentIfNotHandled()?.let { result ->
                showFavoriteNotify(result)
            }
        }
        viewModel.showBetInfoSingle.observe(this) {
            it.getContentIfNotHandled()?.let {
                showBetListPage()
            }
        }
        viewModel.showBetUpperLimit.observe(this) {
            if (it.getContentIfNotHandled() == true) {
                showLoginSnackbar(R.string.bet_notify_max_limit,binding.parlayFloatWindow.id)
            }
        }

        viewModel.showBetBasketballUpperLimit.observe(this) {
            if (it.getContentIfNotHandled() == true) {
                showLoginSnackbar(R.string.bet_basketball_notify_max_limit,binding.parlayFloatWindow.id)
            }
        }

        gamesViewModel.enterThirdGameResult.observe(this) {
            enterThirdGame(it.second, it.first)
        }

        gamesViewModel.gameBalanceResult.observe(this) {
            it.getContentIfNotHandled()?.let { event ->
                TransformInDialog.newInstance(event.first, event.second, event.third).show(supportFragmentManager)
            }
        }

        gamesViewModel.enterTrialPlayGameResult.observe(this) {
            hideLoading()
            if (it == null) {
                //不支持试玩
                startLogin()
            } else {
                //试玩弹框
                val trialDialog = TrialGameDialog(this, it.first, it.second) { firmType, thirdGameResult->
                    enterThirdGame(thirdGameResult, firmType)
                }
                trialDialog.show()
            }
        }
        gamesViewModel.guestLoginGameResult.observe(this) {
            hideLoading()
            if (it == null) {
                //不支持访客
                startLogin()
            } else {
                enterThirdGame(it.second, it.first)
            }
        }

        DataResourceChange.observe(this) { showDataSourceChangedDialog(it) }
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
        binding.parlayFloatWindow?.gone()
    }

    /**
     * 检查是否为体育相关的fragment
     */
    private fun checkSportFragment(): Boolean {
        val fragment = fragmentHelper.getCurrentFragment()
        return fragment is SportFragment || fragment is ESportFragment
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(Bundle()) // 如果回复之前的position会有很多其它崩溃异常
    }

    open fun openDrawerLayout() {
        binding.drawerLayout.openDrawer(Gravity.LEFT)
    }

    fun closeDrawerLayout() {
        binding.drawerLayout.closeDrawer(Gravity.LEFT)
    }

    private fun initDrawerLayout() {
//        binding.drawerLayout.setScrimColor(Color.TRANSPARENT)

        binding.rightMenu.setOnClickListener {  }
        binding.drawerLayout.addDrawerListener(object : SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                if (drawerView.tag == "LEFT") {
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT)
                } else {
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.LEFT)
                }
            }

            override fun onDrawerClosed(drawerView: View) {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        })
    }


    fun showMainLeftMenu(contentFragment: Class<BaseFragment<*,*>>?) {
        onMenuEvent(MenuEvent(true))
        fragmentHelper2.show(MainLeftFragment::class.java, Bundle()) { fragment, _ ->
            fragment.openWithFragment(contentFragment)
        }
    }
    fun showMainRightMenu() {
        fragmentHelperRight.show(MainRightFragment::class.java, Bundle()){ fragment, _ ->
            fragment.reloadData()
        }
    }

    private val fragmentHelper2 by lazy { FragmentHelper2(supportFragmentManager, R.id.left_menu) }
    private val fragmentHelperRight by lazy { FragmentHelper2(supportFragmentManager, R.id.right_menu) }

    fun showSportLeftMenu() {
        onMenuEvent(MenuEvent(true))
        fragmentHelper2.show(SportLeftMenuFragment::class.java, Bundle()) { fragment, instance ->
            if(!instance){
                fragment.reloadData()
            }
        }
    }


    fun initMenu() {
        try {
            //關閉側邊欄滑動行為
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            binding.drawerLayout.setScrimColor(getColor(R.color.transparent_black_20))
//            //選單選擇結束要收起選單
            binding.leftMenu.layoutParams.width = MetricsUtil.getScreenWidth() //動態調整側邊欄寬
            binding.rightMenu.layoutParams.width = MetricsUtil.getScreenWidth()-30.dp //動態調整側邊欄寬

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMenuEvent(event: MenuEvent) {
        if (event.open) {
            binding.drawerLayout.openDrawer(event.gravity)
        } else {
            binding.drawerLayout.closeDrawer(event.gravity)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNetValidEvent(event: NetWorkEvent) {
        //网络恢复
        if (event.isValid) {
            val fragment = fragmentHelper.getFragment(INDEX_HOME)
            if (fragment is HomeFragment) {
                //更新config   刷新体育服务开关
                fragment.viewModel.getConfigData()
            }
        }
    }


    @Subscribe
    fun onBetModeChangeEvent(event: BetModeChangeEvent) {
        if (event.currentMode == BetListFragment.SINGLE) {
            BetInfoRepository.currentBetType = BetListFragment.SINGLE
            binding.parlayFloatWindow.gone()
        } else if (event.currentMode == BetListFragment.BASKETBALL_ENDING_CARD) {
            BetInfoRepository.currentBetType = BetListFragment.BASKETBALL_ENDING_CARD
            if (betListCount != 0) {
                binding.parlayFloatWindow.visible()
            }
        } else {
            BetInfoRepository.currentBetType = BetListFragment.PARLAY
            if (betListCount != 0) {
                binding.parlayFloatWindow.visible()
            }
        }
    }


    //系统方法
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (binding.drawerLayout?.isOpen == true) {
                binding.drawerLayout?.close()
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
            ToastUtil.showToast(this, getString(R.string.str_press_again_to_exit_the_program))
            exitTime = System.currentTimeMillis()
        } else {
            finish()
            exitProcess(0)
        }
    }


   open fun getBetListPageVisible(): Boolean {
        return betListFragment?.isVisible ?: false
    }

    private var betListCount = 0

    fun updateBetListCount(num: Int) {
        betListCount = num
        setupBetBarVisiblity()
        binding.parlayFloatWindow.updateCount(betListCount.toString())
        if (num > 0) viewModel.getMoneyAndTransferOut()
    }


    private fun setupBetBarVisiblity() {

        val needShowBetBar = fragmentHelper.getCurrentPosition() >= 0
                && (fragmentHelper.getCurrentFragment() is HomeFragment
                    || fragmentHelper.getCurrentFragment() is SportFragment
                    || fragmentHelper.getCurrentFragment() is ESportFragment)

        if (betListCount == 0
            || !needShowBetBar
            || BetInfoRepository.currentBetType == BetListFragment.SINGLE) {
            binding.parlayFloatWindow.gone()
            return
        }

        if (BetInfoRepository.currentBetType == BetListFragment.PARLAY) {
            binding.parlayFloatWindow.setBetText(getString(R.string.conspire))
            binding.parlayFloatWindow.updateCount(betListCount.toString())
        } else {
            binding.parlayFloatWindow.setBetText(getString(R.string.F001))
        }
        binding.parlayFloatWindow.visible()
    }

     fun initBottomNavigation() {
        binding.parlayFloatWindow.onViewClick = ::showBetListPage
        val radius = 15.dp.toFloat()
        binding.linTab.background = ShapeDrawable()
            .setWidth(screenWidth + 15.dp)
            .setHeight(58.dp)
            .setSolidColor(Color.WHITE)
            .setShadowColor(getColor(R.color.color_A9B2D3))
            .setShadowSize(5.dp)
            .setShadowOffsetY(-10.dp)
            .setRadius(radius, radius, 0F, 0F)
    }

    fun showBetListPage() {
        betListFragment?.let {
            if (it.isAdded) {
                supportFragmentManager.beginTransaction().apply {
                    remove(it)
                    commitNowAllowingStateLoss()
                }
            }
        }

        betListFragment = BetListFragment.newInstance(object : BetListFragment.BetResultListener {
            override fun onBetResult(
                betResultData: Receipt?, betParlayList: List<ParlayOdd>, isMultiBet: Boolean,
            ) {
                showBetReceiptDialog(betResultData, betParlayList, isMultiBet, R.id.fl_bet_list)
            }
        })


        supportFragmentManager.beginTransaction().add(R.id.fl_bet_list, betListFragment!!).addToBackStack(null).commit()
    }


    fun setupBetData(fastBetDataBean: FastBetDataBean, from: String) {
        viewModel.updateMatchBetListData(fastBetDataBean, from)
    }

    private fun setupBottomNavBarVisibility(isVisible: Boolean) {
        if (betListCount == 0) {
            binding.parlayFloatWindow.gone()
        }
    }


    fun backMainHome() {
        navToPosition(INDEX_HOME)
        tabHelper.selectedHome()
//        tabHelper.clearSelected()
    }

    fun jumpToOKGames() {
        if (getMarketSwitch()) {
            return
        }
        if(StaticData.okGameOpened()){
            tabHelper.selectedGames()
            navToPosition(INDEX_OKGAMES)
        }else{
            ToastUtil.showToast(this,getString(R.string.N700))
        }
    }

    fun jumpToOkLive(){
        if (getMarketSwitch()) {
            return
        }
        if(StaticData.okLiveOpened()){
            tabHelper.clearSelected()
            navToPosition(INDEX_OKLIVE)
        }else{
            ToastUtil.showToast(this,getString(R.string.N700))
        }
    }

    fun jumpToNews() {
        tabHelper.clearSelected()
        navToPosition(INDEX_NEWS)
    }

    private fun navToPosition(position: Int) {
        fragmentHelper.showFragment(position)
    }

    fun jumpToPerya() {
        if (getMarketSwitch()) {
            return
        }

        if(StaticData.miniGameOpened()){
            tabHelper.selectedHome()
            navToPosition(INDEX_HOME)
            val fragment = fragmentHelper.getFragment(INDEX_HOME)
            if (fragment is HomeFragment) {
                fragment.jumpToPerya()
            }
        }else{
            ToastUtil.showToast(this,getString(R.string.N700))
        }
    }

    fun jumpToESport(gameType: String) {
        checkSportStatus(this) {
            tabHelper.clearSelected()
            navToPosition(INDEX_ESPORT)
            (fragmentHelper.getCurrentFragment() as ESportFragment)?.jumpToSport(gameType)
        }
    }

    fun jumpToESport(matchType: MatchType? = null, gameType: String? = null) {
        checkSportStatus(this) {
            tabHelper.clearSelected()
            navToPosition(INDEX_ESPORT)
            binding.root.postDelayed(200) {
                val fragment = fragmentHelper.getCurrentFragment()
                if (fragment is ESportFragment) {
                    fragment.setJumpSport(matchType,gameType)
                }
            }
        }
    }

    fun jumpToSport(gameType: GameType) {
        checkSportStatus(this) {
            tabHelper.selectedSport()
            navToPosition(INDEX_SPORT)
            binding.root.postDelayed(200){
                (fragmentHelper.getFragment(INDEX_SPORT) as SportFragment).jumpToSport(gameType)
            }
        }
    }

    fun jumpToTheSport(matchType: MatchType? = null, gameType: GameType? = null) {
        tabHelper.selectedSport()
        (fragmentHelper.getFragment(INDEX_SPORT) as SportFragment).setJumpSport(matchType, gameType)
        navToPosition(INDEX_SPORT)
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
        loginedRun(this) { startActivity(BetRecordActivity::class.java) }
    }

    /**
     * 跳转到三方游戏指定厂商
     */
    fun jumpOKGameWithProvider(okGamesFirm: OKGamesFirm){
        jumpToOKGames()
        postDelayed(1000){
            (getCurrentFragment() as? OKGamesFragment)?.showByProvider(okGamesFirm)
        }
    }
    /**
     * 跳转到三方真人指定厂商
     */
    fun jumpOKLiveWithProvider(okGamesFirm: OKGamesFirm){
        jumpToOkLive()
        postDelayed(1000){
            (getCurrentFragment() as? OKLiveFragment)?.showByProvider(okGamesFirm)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        OddsButton2.clearOddsViewCaches()
        if (activityInstance == this) {
            activityInstance = null
        }
    }


    fun getCurrentPosition(): Int = fragmentHelper.getCurrentPosition()
    fun getCurrentFragment():Fragment  = fragmentHelper.getCurrentFragment()


    fun enterThirdGame(result: EnterThirdGameResult, firmType: String) {
        hideLoading()
        if (result.okGameBean==null){
            return
        }
        when (result.resultType) {
            EnterThirdGameResult.ResultType.SUCCESS -> {
                JumpUtil.toThirdGameWeb(this, result.url ?: "", firmType, result.okGameBean, result.guestLogin)
                if (result.okGameBean.firmCode!="FKG"){
                    if (LoginRepository.isLogined()&&!OKGamesRepository.isSingleWalletType(firmType) && isThirdTransferOpen()) gamesViewModel.transfer(firmType)
                }
            }

            EnterThirdGameResult.ResultType.FAIL -> showErrorPromptDialog(
                getString(R.string.prompt), result.errorMsg ?: ""
            ) {}

            EnterThirdGameResult.ResultType.NEED_REGISTER -> LoginOKActivity.startRegist(this)

            EnterThirdGameResult.ResultType.GUEST -> showErrorPromptDialog(
                getString(R.string.error), result.errorMsg ?: ""
            ) {}

            EnterThirdGameResult.ResultType.NONE -> {
            }
        }
        if (result.resultType != EnterThirdGameResult.ResultType.NONE) gamesViewModel.clearThirdGame()
    }
    fun enterThirdGame(gameData: OKGameBean,from: String?) {
        from?.let {
            SensorsEventUtil.gameClickEvent(it, "${gameData.firmName}", "${gameData.gameType}", "${gameData.gameName}", gameData.id.toString())
        }

        if(LoginRepository.isLogined()) {
            gamesViewModel.requestEnterThirdGame(gameData, this)
        } else {
            //请求试玩路线
            loading()
            gamesViewModel.requestEnterThirdGameNoLogin(gameData)
        }
    }
    fun collectGame(gameData: OKGameBean, gameEntryType: String? = null): Boolean {
        val gameType = gameEntryType ?: (gameData.gameType?: GameEntryType.OKGAMES)
        return loginedRun(binding.root.context) { gamesViewModel.collectGame(gameData, gameType) }
    }
    private fun jumpGameAfterLogin(){
        if (LoginRepository.isLogined()&&OKGamesRepository.enterGameAfterLogin !=null){
            enterThirdGame(OKGamesRepository.enterGameAfterLogin!!, null)
        }
        OKGamesRepository.enterGameAfterLogin=null
    }
    private fun checkNotificationOpen() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // 检测该应用是否有通知权限
        if (!manager.areNotificationsEnabled()&&KvUtils.decodeBooleanTure(KvUtils.KEY_NOTIFICATION_PERMISSION,true)) {
            KvUtils.put(KvUtils.KEY_NOTIFICATION_PERMISSION,false)
            RxPermissions(this).request(Manifest.permission.POST_NOTIFICATIONS)
                .subscribe { granted ->
                    if (granted) {
                        // All requested permissions are granted
                    } else {
                        // At least one permission is denied
                    }
                }
        }
    }
}
