package org.cxct.sportlottery.ui.maintab

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.activity_main_tab.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.FragmentHelper
import org.cxct.sportlottery.util.MetricsUtil
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainTabActivity : BaseActivity<MainTabViewModel>(MainTabViewModel::class) {

    var fragmentHelper: FragmentHelper? = null
    var fragments = arrayOfNulls<Fragment>(5)

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
    }

    private fun initBottomFragment() {
        fragments[0] = MainHomeFragment.newInstance()
        fragments[1] = MainHomeFragment.newInstance()
        fragments[2] = MainHomeFragment.newInstance()
        fragments[3] = MainHomeFragment.newInstance()
        fragments[4] = MainHomeFragment.newInstance()
        fragmentHelper = FragmentHelper(supportFragmentManager, R.id.fl_content, fragments)
        bottom_navigation_view.apply {
            enableAnimation(true)
            enableShiftingMode(false)
            setTextVisibility(true)
            setTextSize(12f)
            setIconSize(30f)
            onNavigationItemSelectedListener =
                BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
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
                        .statusBarDarkFont(false)
                        .init()
                    setStatusbar(R.color.transparent, false)
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
                    .statusBarDarkFont(true)
                    .init()
            }
        })

    }

    fun initMenu() {
        try {
            //關閉側邊欄滑動行為
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            //選單選擇結束要收起選單
            val fragment =
                supportFragmentManager.findFragmentById(R.id.left_menu) as MainLeftFragment
//            fragment.setDownMenuListener { drawer_layout.closeDrawers() }
            nav_left.layoutParams.width = MetricsUtil.getMenuWidth() //動態調整側邊欄寬

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

}
