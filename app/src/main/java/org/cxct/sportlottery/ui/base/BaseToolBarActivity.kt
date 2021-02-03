package org.cxct.sportlottery.ui.base

import android.os.Bundle
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.android.synthetic.main.activity_base_tool_bar.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.util.MetricsUtil
import kotlin.reflect.KClass

/***
 * 1.目前無法支援使用DataBinding的Activity
 * 2.原本activity onCreate中的setContentView()這行要拿掉
 */


abstract class BaseToolBarActivity<T : BaseViewModel>(claazz: KClass<T>) :
    BaseOddButtonActivity<T>(claazz) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_tool_bar)

        frame_layout.addView(layoutInflater.inflate(resources.getLayout(setContentView()), null))
        tv_toolbar_title.text = setToolBarName()
        initMenu()
    }

    /**
     * 回傳 Layout
     * */
    abstract fun setContentView(): Int

    /**
     * 回傳 標題名稱
     * */
    abstract fun setToolBarName(): String

    /**
     * 回傳 true/false 開/關menu
     * */
    open fun isOpenMenu(isOpen: Boolean) {
        when (isOpen) {
            true -> btn_toolbar_menu.visibility = View.VISIBLE
            false -> btn_toolbar_menu.visibility = View.INVISIBLE
        }
    }

    private fun initMenu() {
        try {
            //選單選擇結束要收起選單
            val menuFrag =
                supportFragmentManager.findFragmentById(R.id.fragment_menu) as MenuFragment
            menuFrag.setDownMenuListener { drawer_layout.closeDrawers() }

            nav_right.layoutParams.width = MetricsUtil.getMenuWidth() //動態調整側邊欄寬
        } catch (e: Exception) {
            e.printStackTrace()
        }

        btn_toolbar_menu.setOnClickListener {
            if (drawer_layout.isDrawerOpen(nav_right)) {
                drawer_layout.closeDrawers()
            } else {
                drawer_layout.openDrawer(nav_right)
            }
        }

        btn_toolbar_back.setOnClickListener {
            drawer_layout.closeDrawers()
            super.onBackPressed()
        }

        //關閉側邊欄滑動行為
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)


    }

    fun setToolBarName(title: String) {
        tv_toolbar_title.text = title
    }
}