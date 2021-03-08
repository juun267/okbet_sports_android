package org.cxct.sportlottery.util

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class ItemNonLastDecoration(private val mDivider: Drawable?) : ItemDecoration() {
    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val dividerLeft = parent.paddingLeft
        val dividerRight = parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 0..childCount - 2) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val dividerTop = child.bottom + params.bottomMargin
            val dividerBottom = mDivider?.let {
                dividerTop + it.intrinsicHeight
            }
            dividerBottom?.let { mDivider?.setBounds(dividerLeft, dividerTop, dividerRight, it) }
            mDivider?.draw(canvas)
        }
    }
}