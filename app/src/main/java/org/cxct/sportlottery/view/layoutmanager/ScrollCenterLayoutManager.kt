package org.cxct.sportlottery.view.layoutmanager

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber


/**
 * Created by iblade.Wang on 2019/5/22 17:08
 * 滾動置中LayoutManager
 */
class ScrollCenterLayoutManager(context: Context?, @RecyclerView.Orientation orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {

    /**
     * 將指定位置的item滾動至中間
     */
    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State, position: Int) {
        val smoothScroller: RecyclerView.SmoothScroller = CenterSmoothScroller(recyclerView.context)
        smoothScroller.targetPosition = position
        try {
            startSmoothScroll(smoothScroller)
        } catch (e: IllegalArgumentException) {
            Timber.e("position: $position, error message: ${e.message}")
        }
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }
    private class CenterSmoothScroller(context: Context?) : LinearSmoothScroller(context) {
        override fun calculateDtToFit(
            viewStart: Int,
            viewEnd: Int,
            boxStart: Int,
            boxEnd: Int,
            snapPreference: Int
        ): Int {
            return boxStart + (boxEnd - boxStart) / 2 - (viewStart + (viewEnd - viewStart) / 2)
        }

        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
            return 100f / (displayMetrics?.densityDpi ?: 0)
        }
    }
}
