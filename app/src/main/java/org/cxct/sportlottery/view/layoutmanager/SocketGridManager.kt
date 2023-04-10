package org.cxct.sportlottery.view.layoutmanager

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 迴避RecyclerView内部BUG: java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid item position
 * 滾動時觸發更新可能會導致此問題而crash
 */
class SocketGridManager(context: Context,
                        spanCount: Int,
                        @RecyclerView.Orientation orientation: Int = RecyclerView.VERTICAL,
                        reverseLayout: Boolean = false) : GridLayoutManager(context, spanCount ,orientation, reverseLayout) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }
}