package org.cxct.sportlottery.view

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.OddsModeUtil

class OkPopupWindow(context: Context, var currentSelectText: String,val onItemClickListener:(String,Int)->Unit ) : PopupWindow(context) {

    private var popupWidth = 0

    private var popupHeight = 0

    private var recyclerView: RecyclerView? = null

    private var parentView: View? = null

    private var lastSelectPosition :Int = 0

    private var listData: MutableList<Pair<String, Boolean>> = mutableListOf(
        Pair(context.getString(R.string.accept_any_change_in_odds), false),
        Pair(context.getString(R.string.accept_better_change_in_odds), false),
        Pair(context.getString(R.string.accept_never_change_in_odds), false),
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
        parentView = View.inflate(context, R.layout.popup_window_list_bet_odds_change, null)
        recyclerView = parentView?.findViewById(R.id.rcv)
        contentView = parentView
        initData(context)
    }

    /**
     * 初始化数据
     *
     * @param context
     */
    private fun initData(context: Context) {
        //给ListView添加数据
        val listAdapter = object : BaseQuickAdapter<Pair<String, Boolean>, BaseViewHolder>(
            R.layout.item_popup_window_list_bet_odds_change, listData
        ) {
            override fun convert(helper: BaseViewHolder, item: Pair<String, Boolean>) {
                helper.setText(R.id.tvOdds, item.first)
                helper.setTextColor(
                    R.id.tvOdds,
                    if (item.first == currentSelectText){
                        lastSelectPosition = helper.layoutPosition
                        context.getColor(R.color.color_025BE8)
                    } else context.getColor(
                        R.color.color_414655
                    )
                )
            }
        }
        recyclerView?.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                DividerItemDecorator(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.divider_gray
                    )
                )
            )

        }

        listAdapter.setOnItemClickListener { adapter, view, position ->
            currentSelectText = listData[position].first
            //刷新当前选中颜色
            listAdapter.notifyItemChanged(position)
            //刷新上一次选中颜色
            listAdapter.notifyItemChanged(lastSelectPosition)
            val descriptionPosition = OddsModeUtil.currentSelectModeIndex(position)
            onItemClickListener(currentSelectText,descriptionPosition)
            dismiss()
        }

    }

    private fun setPopConfig() {
        this.width = ViewGroup.LayoutParams.WRAP_CONTENT
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        // 设置弹出窗体可点击
        this.isFocusable = true
        val dw = parentView?.resources?.getColor(R.color.transparent)?.let { ColorDrawable(it) }
        setBackgroundDrawable(dw)
        this.isOutsideTouchable = true

        //获取自身的长宽高
        parentView!!.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        popupHeight = parentView!!.measuredHeight
        popupWidth = parentView!!.measuredWidth
    }

    /**
     * 设置显示在v上方(以v的左边距为开始位置)
     *
     * @param v
     */
    fun showUpLeft(v: View) {
        //获取需要在其上方显示的控件的位置信息
        val location = IntArray(2)
        v.getLocationOnScreen(location)
        //在控件上方显示
        showAtLocation(
            v, Gravity.NO_GRAVITY, location[0] - popupWidth / 2, location[1] - popupHeight
        )
    }

    /**
     * 设置显示在v上方（以v的中心位置为开始位置）
     *
     * @param v
     */
    fun showUpCenter(v: View) {
        //获取需要在其上方显示的控件的位置信息
        val location = IntArray(2)
        v.getLocationOnScreen(location)
        //在控件上方显示
        showAtLocation(
            v,
            Gravity.NO_GRAVITY,
            location[0] + v.width / 2 - popupWidth / 2,
            location[1] - popupHeight
        )
    }
}