package org.cxct.sportlottery.ui.sport.endcard.bet

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.util.DisplayUtil.dp

class OddsItemDecoration: RecyclerView.ItemDecoration() {

    private val spanCount = 4
    private val horSpacing = 0
    private val verSpacing = 8.dp
    private val sideSpacing = 12.dp
    private val mDivider = ColorDrawable(Color.TRANSPARENT)
    private val lineHeight = 1
    private val divideHeight = 24.dp

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {

        val position = parent.getChildAdapterPosition(view)
        var topMargin = if (position < spanCount) verSpacing + sideSpacing else verSpacing
        var bottomMargin = if (needSetDivide(position)) verSpacing + divideHeight else verSpacing

        when (position % spanCount) {
            0 -> {
                outRect.set(sideSpacing, topMargin, 0, bottomMargin)
            }
            spanCount - 1 -> {
                outRect.set(horSpacing, topMargin, sideSpacing, bottomMargin)
            }
            else -> {
                outRect.set(horSpacing, topMargin, 0, bottomMargin)
            }
        }

    }
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (needSetDivide(position)){
                val left = 20.dp
                val right = c.width - 20.dp
                val top = child.bottom +(verSpacing+divideHeight+lineHeight)/2
                val bottom = top + lineHeight
                mDivider.setBounds(left, top, right, bottom)
                mDivider.draw(c)
            }else{
                super.onDraw(c, parent, state)
            }
        }
    }

    private fun RecyclerView.getItemOddOrNull(childView: View): Odd?{
        val position = getChildAdapterPosition(childView)
        val adapter=adapter as BaseQuickAdapter<*, *>
        return adapter.getItemOrNull(position) as? Odd
    }
    /**
     * 当前实际position
     */
    private fun RecyclerView.indexOfParent(childView: View): Int{
        return getChildAdapterPosition(childView)
    }
    /**
     * 是否需要设置分割线
     */
    private fun needSetDivide(position: Int): Boolean{
        return position / spanCount % 2 == 1
    }
}