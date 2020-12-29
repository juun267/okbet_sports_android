package org.cxct.sportlottery.util

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class SpaceItemDecoration(private val context: Context, private val dimenRes: Int) :
    RecyclerView.ItemDecoration() {

    private val space by lazy {
        context.resources.getDimensionPixelOffset(dimenRes)
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        when (getOrientation(parent)) {
            LinearLayoutManager.VERTICAL -> if (position != 0) outRect.top = space
            LinearLayoutManager.HORIZONTAL -> if (position != 0) outRect.left = space
        }
    }

    private fun getOrientation(parent: RecyclerView): Int {
        val layoutManager = parent.layoutManager
        return if (layoutManager is LinearLayoutManager) {
            layoutManager.orientation
        } else -1
    }
}