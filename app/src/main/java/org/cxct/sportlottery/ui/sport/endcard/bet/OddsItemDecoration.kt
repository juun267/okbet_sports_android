package org.cxct.sportlottery.ui.sport.endcard.bet

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.DisplayUtil.dp
import timber.log.Timber

class OddsItemDecoration(context: Context): RecyclerView.ItemDecoration() {

    private val spanCount = 4
    private val horSpacing = 5.dp
    private val verSpacing = 4.dp
    private val mDivider = ColorDrawable(context.getColor(R.color.color_1A2C38))
    private val lineHeight = 2.dp
    private val divideHeight = 18.dp

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val position = parent.getChildAdapterPosition(view)
        var topMargin = verSpacing/2
        var bottomMargin = if (needSetDivide(position)) verSpacing/2 + divideHeight else verSpacing/2

        when (position % spanCount) {
            0 -> {
                outRect.set(horSpacing/2, topMargin, horSpacing/2, bottomMargin)
            }
            spanCount - 1 -> {
                outRect.set(horSpacing/2, topMargin, horSpacing/2, bottomMargin)
            }
            else -> {
                outRect.set(horSpacing/2, topMargin, horSpacing/2, bottomMargin)
            }
        }
    }
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (needSetDivide(position)){
                val left = 0.dp
                val right = c.width - left
                val top = child.bottom +(verSpacing+divideHeight-lineHeight)/2
                val bottom = top + lineHeight
                mDivider.setBounds(left, top, right, bottom)
                mDivider.draw(c)
            }else{
                super.onDraw(c, parent, state)
            }
        }
    }

    /**
     * 是否需要设置分割线
     */
    private fun needSetDivide(position: Int): Boolean{
        val rowIndex = position / spanCount
        return rowIndex==4 || rowIndex==14
    }
}