package org.cxct.sportlottery.ui.maintab

import BetRecordFragment
import android.content.Intent
import android.graphics.Color
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
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavActivity
import org.cxct.sportlottery.ui.game.betList.BetListFragment
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.main.MainActivity
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterFragment
import org.cxct.sportlottery.ui.sport.favorite.FavoriteFragment
import org.cxct.sportlottery.util.FragmentHelper
import org.cxct.sportlottery.util.MetricsUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.observe
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber


class MainTabActivity : BaseBottomNavActivity<MainTabViewModel>(MainTabViewModel::class) {

    lateinit var fragmentHelper: FragmentHelper
    var fragments = arrayOf<Fragment>(
        MainHomeFragment.newInstance(),
        SportFragment.newInstance(),
        BetRecordFragment.newInstance(),
        FavoriteFragment.newInstance(),
        ProfileCenterFragment.newInstance()
    )
    private var betListFragment = BetListFragment()

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
    }

    private fun initBottomFragment() {
        fragmentHelper = FragmentHelper(supportFragmentManager, R.id.fl_content, fragments)
        bottom_navigation_view.apply {
            enableAnimation(true)
            enableShiftingMode(false)
            setTextVisibility(true)
            setTextSize(12f)
            setIconSize(30f)
            onNavigationItemSelectedListener =
                BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.i_betlist, R.id.i_favorite, R.id.i_user -> {
                            if (viewModel.isLogin?.value == false) {
                                startActivity(Intent(this@MainTabActivity,
                                    LoginActivity::class.java))
                                false
                            }
                        }

                    }
                    val itemId = menuItem.itemId
                    fragmentHelper?.showFragment(this.getMenuItemPosition(menuItem))
                    true
                }
        }
        bottom_navigation_view.currentItem = 0
    }

    open fun openDrawerLayout() {
        drawerLayout.openDrawer(Gravity.LEFT)
    }

    private fun initDrawerLayout() {
        drawerLayout.setScrimColor(Color.TRANSPARENT)
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                val mContent: View = drawerLayout.getChildAt(0)
                //设置1.1，让主界面更缩小
                val scale = 1 - slideOffset
                val rightScale = 0.5f + scale * 0.5f
                if (drawerView.tag == "LEFT") {
                    val leftScale = 1 - scale
                    drawerView.scaleX = leftScale
                    drawerView.scaleY = leftScale
                    drawerView.alpha = 1f
                    mContent.translationX = drawerView.measuredWidth * (1 - scale)
                    mContent.pivotX = 0f
                    mContent.pivotY = (mContent.measuredHeight / 2).toFloat()
                    mContent.invalidate()
                    //以下代码是仿QQ效果
                    mContent.scaleX = rightScale
                    mContent.scaleY = rightScale
                } else {
                    mContent.translationX = -(drawerView.measuredWidth * slideOffset)
                    mContent.pivotX = mContent.measuredWidth.toFloat()
                    mContent.pivotY = (mContent.measuredHeight / 2).toFloat()
                    mContent.invalidate()
                    //以下代码是仿QQ效果
                    mContent.scaleX = rightScale
                    mContent.scaleY = rightScale
                }
            }

            override fun onDrawerOpened(drawerView: View) {
                if (drawerView.tag == "LEFT") {
                    drawerLayout.setDrawerLockMode(
                        DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                        Gravity.RIGHT
                    )
                    ImmersionBar.with(this@MainTabActivity)
                        .transparentStatusBar()
                        .statusBarDarkFont(false)
                        .fitsSystemWindows(false)
                        .init()
                } else {
                    drawerLayout.setDrawerLockMode(
                        DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                        Gravity.LEFT
                    )
                }
            }

            override fun onDrawerClosed(drawerView: View) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                ImmersionBar.with(this@MainTabActivity)
                    .transparentStatusBar()
                    .statusBarDarkFont(true)
                    .fitsSystemWindows(false)
                    .init()
            }
        })

    }

    override fun initMenu() {
        try {
            //關閉側邊欄滑動行為
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            //選單選擇結束要收起選單
            val fragment =
                supportFragmentManager.findFragmentById(R.id.left_menu) as MainLeftFragment
            left_menu.layoutParams.width = MetricsUtil.getScreenWidth() / 5 * 4 //動態調整側邊欄寬

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

    override fun updateBetListCount(num: Int) {
        cl_bet_list_bar.isVisible = num > 0
        cl_bet_list_bar.tv_bet_list_count.text = num.toString()
        Timber.e("num: $num")
        if (num > 0) viewModel.getMoney()
    }

    override fun showLoginNotify() {

    }

    override fun showMyFavoriteNotify(myFavoriteNotifyType: Int) {

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
                    isMultiBet: Boolean
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
}
