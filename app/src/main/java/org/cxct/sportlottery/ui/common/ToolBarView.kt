package org.cxct.sportlottery.ui.common

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.view_tool_bar_drawer_layout.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.menu.MenuFragment
import org.cxct.sportlottery.util.MetricsUtil

class ToolBarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    //    lateinit var drawerLayout: DrawerLayout

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_tool_bar_drawer_layout, this, false)
        val typeArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ToolBarView, 0, 0)
        view.apply {
            tv_toolbar_title.text = typeArray.getText(R.styleable.ToolBarView_title)
            setNavGraph(typeArray.getResourceId(R.styleable.ToolBarView_fragmentNavGraph, -1))
        }
        addView(view)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initMenu()
    }

    private fun setNavGraph(navigation : Int = R.navigation.bet_record_navigation) {
        val myNavHostFragment: NavHostFragment = (context as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment
        val graph = myNavHostFragment.navController.navInflater.inflate(navigation)
        myNavHostFragment.navController.graph = graph
    }

    /*
        private fun addDrawerLayout() {
            drawerLayout = DrawerLayout(context)
            val fl = FrameLayout(context)
            fl.id = R.id.frame_layout
            val navList = ListView(context)

            val lp = DrawerLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

            lp.gravity = Gravity.START

            navList.layoutParams = lp

            drawerLayout.addView(fl, FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            drawerLayout.addView(navList)

            addView(drawerLayout)
        }
    */

    private fun initMenu() {
        try {
            //選單選擇結束要收起選單
            val menuFrag = (context as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.fragment_menu) as MenuFragment
            menuFrag.setDownMenuListener { drawer_layout.closeDrawers() }

            nav_right.layoutParams.width = MetricsUtil.getMenuWidth() //動態調整側邊欄寬
        } catch (e: Exception) {
            e.printStackTrace()
        }

        btn_toolbar_menu.setOnClickListener {
            if (drawer_layout.isDrawerOpen(nav_right)) drawer_layout.closeDrawers()
            else {
                drawer_layout.openDrawer(nav_right)
            }
        }

        btn_toolbar_back.setOnClickListener {
            if (context is Activity) {
                (context as Activity).onBackPressed()
            }
        }

    }

    fun setTitle(title: String) {
        tv_toolbar_title.text = title
    }
}