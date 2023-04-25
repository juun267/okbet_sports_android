package org.cxct.sportlottery.ui.chat

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Recycler

class CustomLinearLayoutManager(
    context: Context?,
    orientation: Int = VERTICAL,
    reverseLayout: Boolean = false,
) : LinearLayoutManager(context, orientation, reverseLayout) {
    private var isScrollEnabled = true

    fun setScrollEnabled(flag: Boolean) {
        this.isScrollEnabled = flag
    }

    override fun canScrollVertically(): Boolean {
        return isScrollEnabled && super.canScrollVertically()
    }

    override fun canScrollHorizontally(): Boolean {
        return isScrollEnabled && super.canScrollHorizontally()
    }

    override fun onInterceptFocusSearch(focused: View, direction: Int): View? {
        return super.onInterceptFocusSearch(focused, direction)
    }


    /**
     * 處理 RecycleView 遇到 IndexOutOfBoundsException: Inconsistency detected. 問題
     * https://www.jianshu.com/p/2eca433869e9
     */
    override fun onLayoutChildren(
        recycler: Recycler?,
        state: androidx.recyclerview.widget.RecyclerView.State?,
    ) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }
}
