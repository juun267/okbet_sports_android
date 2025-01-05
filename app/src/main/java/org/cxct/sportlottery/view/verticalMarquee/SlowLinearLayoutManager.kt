package org.cxct.sportlottery.view.verticalMarquee

import android.content.Context
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SlowLinearLayoutManager(context: Context?, @RecyclerView.Orientation orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout){

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State?, position: Int) {
        val smoothScroller = object : LinearSmoothScroller(recyclerView.context) {
            override fun calculateSpeedPerPixel(displayMetrics: android.util.DisplayMetrics): Float {
                return 500f / displayMetrics.densityDpi // 设置滚动速度
            }
        }
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }
}
