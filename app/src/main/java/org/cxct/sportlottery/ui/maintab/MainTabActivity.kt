package org.cxct.sportlottery.ui.maintab

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PathMeasure
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.gyf.immersionbar.ImmersionBar
import com.luck.picture.lib.tools.ToastUtils
import kotlinx.android.synthetic.main.activity_main_tab.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.event.BetModeChangeEvent
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ActivityMainTabBinding
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.bet.settledList.Row
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.ui.base.BaseBottomNavActivity
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.betList.BetListFragment
import org.cxct.sportlottery.ui.betRecord.BetRecordFragment
import org.cxct.sportlottery.ui.betRecord.accountHistory.next.AccountHistoryNextFragment
import org.cxct.sportlottery.ui.maintab.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.maintab.home.HomeFragment
import org.cxct.sportlottery.ui.maintab.menu.MainLeftFragment2
import org.cxct.sportlottery.ui.maintab.menu.SportLeftFragment
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterFragment
import org.cxct.sportlottery.ui.sport.SportFragment
import org.cxct.sportlottery.ui.sport.favorite.FavoriteFragment
import org.cxct.sportlottery.ui.sport.list.SportLeagueAdapter
import org.cxct.sportlottery.util.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.system.exitProcess


class MainTabActivity : BaseBottomNavActivity<MainTabViewModel>(MainTabViewModel::class) {

    private val fragmentHelper: FragmentHelper by lazy {
        FragmentHelper(
            supportFragmentManager, R.id.fl_content, arrayOf(
                Pair(HomeFragment::class.java, null),
                Pair(SportFragment::class.java, null),
                Pair(BetRecordFragment::class.java, null),
                Pair(FavoriteFragment::class.java, null),
                Pair(ProfileCenterFragment::class.java, null)
            )
        )
    }

    private var betListFragment: BetListFragment? = null
    private val homeLeftFragment by lazy { MainLeftFragment2() }
    private val sportLeftFragment by lazy { SportLeftFragment() }
    private var exitTime: Long = 0

    companion object {

        var activityInstance: MainTabActivity? = null

        fun reStart(context: Context) {
            val intent = Intent(context, MainTabActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    private val binding by lazy { ActivityMainTabBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        SportLeagueAdapter.clearCachePool()
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
    }

    override fun onNightModeChanged(mode: Int) {
        super.onNightModeChanged(mode)
        reStart(this)
    }

    private fun initObserve() {
//        viewModel.userMoney.observe(this) {
//            it?.let { money ->
////                cl_bet_list_bar.tv_balance.text = TextUtil.formatMoney(money)
//            }
//        }
        viewModel.showBetInfoSingle.observe(this) {
            it.getContentIfNotHandled()?.let {
                showBetListPage()
            }
        }
        viewModel.showBetUpperLimit.observe(this) {
            if (it.getContentIfNotHandled() == true) snackBarBetUpperLimitNotify.apply {
                setAnchorView(R.id.parlayFloatWindow)
                show()
            }
        }
    }

    private fun initBottomFragment(position: Int) {
        binding.llHomeBack.setOnClickListener {
            homeFragment().backMainHome()
        }
        binding.bottomNavigationView.apply {
            enableAnimation(false)
            enableShiftingMode(false)
            setTextVisibility(true)
            setTextSize(10f)
            setIconSize(30f)
            menu.getItem(2).isVisible = !SPUtil.getMarketSwitch()
            onNavigationItemSelectedListener =
                BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.i_betlist, R.id.i_favorite, R.id.i_user -> {
                            if (viewModel.isLogin.value == false) {
                                startLogin()
                                return@OnNavigationItemSelectedListener false
                            }
                        }
                    }

                    val position = getMenuItemPosition(menuItem)
                    fragmentHelper.showFragment(position)
                    if (position == 0) {
                        homeFragment().backMainHome()
                    } else {
                        binding.llHomeBack.gone()
                    }
                    setupBetBarVisiblity(position)
                    return@OnNavigationItemSelectedListener true
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

    var menuClass: Class<*>? = null

    fun showMainLeftMenu(contentFragment: BaseFragment<*>?) {
        if (menuClass != homeLeftFragment::class.java) {
            menuClass = homeLeftFragment::class.java
            left_menu.layoutParams.width = MetricsUtil.getScreenWidth()
        }
        homeLeftFragment.openWithFragment(contentFragment)
        supportFragmentManager.beginTransaction()
            .replace(R.id.left_menu, homeLeftFragment)
            .commit()
    }

    fun showSportLeftMenu(matchType: MatchType, gameType: GameType?) {
        if (menuClass != sportLeftFragment::class.java) {
            menuClass = sportLeftFragment::class.java
            left_menu.layoutParams.width = (MetricsUtil.getScreenWidth() * 0.75f).toInt()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.left_menu, sportLeftFragment)
            .commit()
        sportLeftFragment.matchType = matchType
        sportLeftFragment.gameType = gameType
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


    @Subscribe
    fun onBetModeChangeEvent(event: BetModeChangeEvent) {
        if (event.currentMode == BetListFragment.SINGLE) {
            BetInfoRepository.currentBetType = BetListFragment.SINGLE
            parlayFloatWindow.gone()
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

    private var betListCount = 0

    override fun updateBetListCount(num: Int) {
        betListCount = num
        setupBetBarVisiblity(bottom_navigation_view.currentItem)
        parlayFloatWindow.tv_bet_list_count.text = betListCount.toString()
        if (num > 0) viewModel.getMoneyAndTransferOut()
    }


    private fun setupBetBarVisiblity(position: Int) {
        val needShowBetBar = when (position) {
            0, 1, 3 -> true
            else -> false
        }

        if (betListCount == 0 || !needShowBetBar || BetInfoRepository.currentBetType == BetListFragment.SINGLE) {
//            Timber.d("ParlayFloatWindow隐藏：betListCount:${betListCount} !needShowBetBar:${!needShowBetBar} currentBetMode:${BetInfoRepository.currentBetType}")
            parlayFloatWindow.gone()
        } else {
            if (BetInfoRepository.currentBetType == BetListFragment.PARLAY) {
//                Timber.d("ParlayFloatWindow显示")
                parlayFloatWindow.visible()
            }
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

    private val mCurrentPosition = FloatArray(2)

    fun getViewsScreenShot(v: View): Bitmap? {
        v.isDrawingCacheEnabled = true
        v.buildDrawingCache()
        return v.drawingCache
    }

    private fun addAction(view: View) {
        if (binding.parlayFloatWindow.isGone) {
            return
        }
        // 一 、创建购物的ImageView 添加到父布局中
        val imageView = ImageView(this)
//        imageView.setImageBitmap(getViewsScreenShot(view))
        imageView.setImageResource(R.drawable.ic_home_football_sel)
        val params: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(60, 60)
        llRootView.addView(imageView, params)
        // 二 、起点位置\终点的坐标\父布局控制点坐标
        val startA = IntArray(2)
        view.getLocationInWindow(startA)
        // 获取终点的坐标
        val endB = IntArray(2)
        binding.parlayFloatWindow.tv_bet_list_count.getLocationInWindow(endB)
        // 父布局控制点坐标
        val parentC = IntArray(2)
        llRootView.getLocationInWindow(parentC)
        //        三、正式开始计算动画开始/结束的坐标
        //开始掉落的商品的起始点：商品起始点-父布局起始点+该商品图片的一半
        val startX = (startA[0] - parentC[0]).toFloat()
        val startY = (startA[1] - parentC[1]).toFloat()
        //商品掉落后的终点坐标：购物车起始点-父布局起始点+购物车图片的1/5
        val toX = (endB[0] - parentC[0]).toFloat()
        val toY = (endB[1] - parentC[1]).toFloat()
        // 四、计算中间动画的插值坐标（贝塞尔曲线）（其实就是用贝塞尔曲线来完成起终点的过程）
        //开始绘制贝塞尔曲线
        val path = android.graphics.Path()
        //移动到起始点（贝塞尔曲线的起点）
        path.moveTo(startX, startY)
        //使用二次萨贝尔曲线：注意第一个起始坐标越大，贝塞尔曲线的横向距离就会越大，一般按照下面的式子取即可
        path.quadTo((startX + toX) / 2, startY, toX, toY)
        //mPathMeasure用来计算贝塞尔曲线的曲线长度和贝塞尔曲线中间插值的坐标，
        // 如果是true，path会形成一个闭环
        val mPathMeasure = PathMeasure(path, false)
        //★★★属性动画实现（从0到贝塞尔曲线的长度之间进行插值计算，获取中间过程的距离值）
        val valueAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, mPathMeasure.length)
        valueAnimator.duration = 500
        // 匀速线性插值器
        valueAnimator.setInterpolator(LinearInterpolator())
        valueAnimator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator) {
                // 这里这个值是中间过程中的曲线长度（下面根据这个值来得出中间点的坐标值）
                val value = animation.getAnimatedValue() as Float
                // ★★★★★获取当前点坐标封装到mCurrentPosition
                // 离的坐标点和切线，pos会自动填充上坐标，这个方法很重要。
                mPathMeasure.getPosTan(
                    value, mCurrentPosition, null
                ) //mCurrentPosition此时就是中间距离点的坐标值
                // 移动的商品图片（动画图片）的坐标设置为该中间点的坐标
                imageView.translationX = mCurrentPosition[0]
                imageView.translationY = mCurrentPosition[1]
            }
        })
        //五、 开始执行动画
        valueAnimator.start()
        //六、动画结束后的处理
        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {}

            //当动画结束后：
            override fun onAnimationEnd(animation: Animator?) {
                // 购物车的数量加1
//                count++
//                tvGoodNum.setText(count + "")
                // 把移动的图片imageview从父布局里移除
                llRootView.removeView(imageView)
//                cl_bet_list_bar.tv_bet_list_count.text = betListCount.toString()
                // 开始一个放大动画
//                val scaleAnim: Animation =
//                    AnimationUtils.loadAnimation(this@FoodActivity2, R.anim.shop_scale)
//                ivGoodsCar.startAnimation(scaleAnim)
            }

            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {}
        })
    }

    fun goBetRecordDetails(bean: Row, date: String, gameType: String) {
        setupBottomNavBarVisibility(false)
        supportFragmentManager.beginTransaction()
            .add(R.id.fl_content, AccountHistoryNextFragment.newInstance(bean, date, gameType))
            .addToBackStack(AccountHistoryNextFragment::class.java.simpleName).commit()
    }

    fun jumpToTheSport(matchType: MatchType, gameType: GameType) {
        resetBackIcon(1)
        (fragmentHelper.getFragment(1) as SportFragment).setJumpSport(matchType, gameType)
    }

    private fun resetBackIcon(position: Int) {
        if (bottom_navigation_view.currentItem != position) {
            bottom_navigation_view.currentItem = position
        }
    }

    private inline fun homeFragment() = fragmentHelper.getFragment(0) as HomeFragment

    fun backMainHome() {
        resetBackIcon(0)
        homeFragment().backMainHome()
    }

    fun jumpToLive() {
        resetBackIcon(0)
        homeFragment().jumpToLive()
    }

    fun jumpToOKGames() {
        resetBackIcon(0)
        homeFragment().jumpToOKGames()
    }

    fun jumpToInplaySport(){
        resetBackIcon(1)
        ll_home_back.gone()
        jumpToTheSport(MatchType.IN_PLAY, GameType.ALL)
    }

    fun jumpToEarlySport() {
        resetBackIcon(1)
        ll_home_back.gone()
        jumpToTheSport(MatchType.EARLY, GameType.FT)
    }

    fun jumpToBetInfo(tabPosition: Int) {
        if (SPUtil.getMarketSwitch()) {
            return
        }
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
            activityInstance = null
        }
        SportLeagueAdapter.clearCachePool()
    }

    override fun updateBetListOdds(list: MutableList<BetInfoListData>) {
    }
}
