package org.cxct.sportlottery.ui.game.betList

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.ui.game.betList.adapter.BetListRefactorAdapter
import org.cxct.sportlottery.util.DisplayUtil.dpToPx

/**
 * 动态设置RecyclerView高度的工具类
 */
object BetListRcvUtil {

    /**
     * 根据展开收起状态，动态设置RCV的高度
     * isOpen-true  : 设置为包裹内容
     * isOpen-false : 设置为单个条目的高度
     */
    fun setFitHeight(
        isOpen: Boolean, recyclerView: RecyclerView, adapter: BetListRefactorAdapter
    ) {
        val itemCount = adapter.getListSize()
        val measuredHeight = getMeasuredHeight(itemCount, recyclerView, adapter)
        val layoutParams = LinearLayoutCompat.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            if (isOpen) measuredHeight else measuredHeight * itemCount
        )
        recyclerView.layoutParams = layoutParams
    }

    fun setWrapHeight(recyclerView: RecyclerView, adapter: BetListRefactorAdapter) {
        val itemCount = adapter.getListSize()
        val measuredHeight = getMeasuredHeight(itemCount, recyclerView, adapter)
        val layoutParams = LinearLayoutCompat.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            measuredHeight * itemCount  /*这个 123f 为键盘高度 @see org.cxct.sportlottery.util.KeyboardView */
        )
        recyclerView.layoutParams = layoutParams
    }

    private fun getMeasuredHeight(
        itemCount: Int, recyclerView: RecyclerView, adapter: BetListRefactorAdapter
    ): Int {
        var measuredHeight = 0
        if (itemCount > 0) {
            val holder = adapter.createViewHolder(
                recyclerView, adapter.getItemViewType(0)
            )
            adapter.onBindViewHolder(holder, 0)
            holder.itemView.measure(
                View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            holder.itemView.layout(
                0, 0, holder.itemView.measuredWidth, holder.itemView.measuredHeight
            )
            measuredHeight = holder.itemView.measuredHeight
        }
        return measuredHeight
    }





}