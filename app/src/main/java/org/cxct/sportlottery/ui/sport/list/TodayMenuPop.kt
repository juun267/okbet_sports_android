package org.cxct.sportlottery.ui.sport.list

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.toBinding
import org.cxct.sportlottery.databinding.HomeCateTabBinding
import org.cxct.sportlottery.databinding.ItemTodayMenuBinding

class TodayMenuPop(val context: Activity, var lastSelectPosition :Int = 0, val onItemClickListener: (position:Int) -> Unit) : PopupWindow(context) {

    private var parentView: View? = null
    private var rvMenu: RecyclerView? = null

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
    private val todayMenuAdapter by lazy { TodayMenuAdapter().apply {
        setList(names)
        setOnItemClickListener{ adapter, view, position ->
            lastSelectPosition = position
            (adapter as TodayMenuAdapter).selectPos = position
            onItemClickListener.invoke(position)
            todayTabItem?.customView?.toBinding<HomeCateTabBinding>()?.apply {
                tvTitle.text = names[lastSelectPosition]
                tvNumber.text = counts[lastSelectPosition].toString()
            }
            dismiss()
        }
    } }

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
        rvMenu = parentView?.findViewById(R.id.rvMenu)
        rvMenu?.layoutManager = LinearLayoutManager(context,RecyclerView.VERTICAL,false)
        rvMenu?.adapter = todayMenuAdapter
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
            todayTabItem?.customView?.toBinding<HomeCateTabBinding>()?.ivArrow?.setImageResource(R.drawable.ic_arrow_gray_down)
        }
    }
    open fun updateCount(vararg countOfType:Int){
        counts = countOfType.toMutableList()
        todayMenuAdapter.countArr=counts
        todayTabItem?.customView?.toBinding<HomeCateTabBinding>()?.apply {
            tvTitle.text = names[lastSelectPosition]
            tvNumber.text = counts[lastSelectPosition].toString()
        }
    }
    override fun showAsDropDown(anchor: View?) {
        super.showAsDropDown(anchor)
        todayMenuAdapter.selectPos = if(todayTabItem?.isSelected==true) lastSelectPosition else -1
        todayTabItem?.customView?.toBinding<HomeCateTabBinding>()?.ivArrow?.setImageResource(R.drawable.ic_arrow_blue_up)
    }

     class TodayMenuAdapter:BindingAdapter<String,ItemTodayMenuBinding>(){
         var countArr: MutableList<Int>?=null
             set(value) {
                 field = value
                 notifyDataSetChanged()
             }
         var selectPos =-1
             set(value) {
                 field = value
                 notifyDataSetChanged()
             }

        override fun onBinding(position: Int, binding: ItemTodayMenuBinding, item: String) {
            binding.tvName.text = item
            binding.tvNum.text = countArr?.get(position).toString()
            binding.root.isSelected = selectPos==position
        }

    }
}