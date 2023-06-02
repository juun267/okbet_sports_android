package org.cxct.sportlottery.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView


/**
 * @author kevin
 * @create 2023/3/17
 * @description
 */
@SuppressLint("WrongConstant")
class SmoothLinearLayoutManager(
    context: Context?,
    orientation: Int = VERTICAL,
    reverseLayout: Boolean = false,
) : LinearLayoutManager(context, orientation, reverseLayout) {
    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State?,
        position: Int,
    ) {
        val smoothScroller: LinearSmoothScroller =
            object : LinearSmoothScroller(recyclerView.context) {
                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                    return 150f / displayMetrics.densityDpi
                }
            }
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }
}