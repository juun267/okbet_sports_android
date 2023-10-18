package org.cxct.sportlottery.ui.sport

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.get
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.home_cate_tab.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.LogUtil

class TodayMenuPop(val context: Activity,val onItemClickListener: (position:Int) -> Unit) : PopupWindow(context) {

    private var parentView: View? = null
    private var rgTodayMenu: RadioGroup? = null
    var lastSelectPosition :Int = 0
    var todayTabItem:TabLayout.Tab? = null
    private var names = listOf(
        context.getString(R.string.home_tab_today),
        context.getString(R.string.home_tab_at_start),
        context.getString(R.string.P228),
        context.getString(R.string.P229),
    )
    private var counts = mutableListOf<Int>(
        0,
        0,
        0,
        0,
    )

    init {
        initView(context)
        setPopConfig()
    }

    /**
     * 初始化控件
     *
     * @param context
     */
    private fun initView(context: Context) {
        parentView = View.inflate(context, R.layout.pop_today_menu, null)
        contentView = parentView
        rgTodayMenu = parentView?.findViewById(R.id.rgTodayMenu)
        rgTodayMenu?.setOnCheckedChangeListener { group, checkedId ->
           val position = when(checkedId){
               R.id.rbtnToday->0
               R.id.rbtnSoon->1
               R.id.rbtnNext12->2
               R.id.rbtnNext24->3
               else->0
           }
            lastSelectPosition = position
            LogUtil.d("lastSelectPosition="+lastSelectPosition)
            onItemClickListener.invoke(position)
            todayTabItem?.customView?.apply {
                tv_title.text = names[lastSelectPosition]
                tv_number.text = counts[lastSelectPosition].toString()
            }
            dismiss()
        }
        parentView?.setOnClickListener {
            dismiss()
        }
    }
    private fun setPopConfig() {
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        isFocusable = true
        elevation = 0f
        setBackgroundDrawable(null)
        setOnDismissListener {
            todayTabItem?.customView?.ivArrow?.rotation = 0f
        }
    }
    open fun updateCount(vararg countOfType:Int){
        counts = countOfType.toMutableList()
        (rgTodayMenu?.getChildAt(0) as TextView).text = context.getString(R.string.home_tab_today)+" "+counts[0].toString()
        (rgTodayMenu?.getChildAt(1) as TextView).text = context.getString(R.string.home_tab_at_start)+" "+counts[1].toString()
        (rgTodayMenu?.getChildAt(2) as TextView).text = context.getString(R.string.P228)+" "+counts[2].toString()
        (rgTodayMenu?.getChildAt(3) as TextView).text = context.getString(R.string.P229)+" "+counts[3].toString()
        todayTabItem?.customView?.apply {
            tv_title.text = names[lastSelectPosition]
            tv_number.text = counts[lastSelectPosition].toString()
        }
    }
    override fun showAsDropDown(anchor: View?) {
        super.showAsDropDown(anchor)
        todayTabItem?.customView?.ivArrow?.rotation = 180f
    }
    private fun backgroundAlpha(bgAlpha: Float) {
        context.window.attributes.apply {
            alpha = bgAlpha
            context.window.attributes = this
        }
    }
}