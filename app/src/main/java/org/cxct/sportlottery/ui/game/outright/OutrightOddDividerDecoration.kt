package org.cxct.sportlottery.ui.game.outright

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView

class OutrightOddDividerDecoration(divider: Drawable?) : RecyclerView.ItemDecoration() {

    private val mDivider: Drawable? = divider

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (mDivider == null) return

        val dividerLeft = parent.paddingLeft
        val dividerRight = parent.width - parent.paddingRight
        val childCount = parent.childCount

        for (i in 0..childCount - 2) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            val viewType = parent.adapter?.getItemViewType(position)
            val nextViewType =
                if (position + 1 < parent.adapter?.itemCount ?: 0) {
                    parent.adapter?.getItemViewType(
                        position + 1
                    )
                } else {
                    null
                }
            val params = child.layoutParams as RecyclerView.LayoutParams
            val dividerTop = child.bottom + params.bottomMargin
            val dividerBottom: Int = dividerTop + mDivider.intrinsicHeight

            if (viewType == OutrightOddAdapter.ItemType.ODD.ordinal &&
                (nextViewType != null && nextViewType != OutrightOddAdapter.ItemType.SUB_TITLE.ordinal)
            ) {
                mDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
                mDivider.draw(canvas)
            }
        }
    }
}