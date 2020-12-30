package org.cxct.sportlottery.ui.common

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_tool_bar.*
import kotlinx.android.synthetic.main.view_tool_bar.view.*
import org.cxct.sportlottery.R

class ToolBarDrawerLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : DrawerLayout(context, attrs, defStyle)  {

    lateinit var drawerLayout: DrawerLayout

    init {
        Log.e(">>>", "init")
        val view = LayoutInflater.from(context).inflate(R.layout.view_tool_bar_drawer_layout, this, false)
        addView(view)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
//        addDrawerLayout()
        initMenu()
    }

    private fun addDrawerLayout() {
        drawerLayout = DrawerLayout(context)
        val fl = FrameLayout(context)
        fl.id = R.id.drawer_layout
        val navList = ListView(context)

        val lp = DrawerLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        lp.gravity = Gravity.START

        navList.layoutParams = lp

        drawerLayout.addView(fl, FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        drawerLayout.addView(navList)

        addView(drawerLayout)
    }

    private fun initMenu() {
        btn_toolbar_back.setOnClickListener {
            if (context is Activity) {
                (context as Activity).onBackPressed()
            }
        }
/*

        btn_toolbar_menu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(drawer_layout))
                drawerLayout.closeDrawers()
            else {
                drawerLayout.openDrawer(nav_right)
            }
        }
*/

    }

    fun setTitle(title: String) {
        tv_toolbar_title.text = title
    }
}