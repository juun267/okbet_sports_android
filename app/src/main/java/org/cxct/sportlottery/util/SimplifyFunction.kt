package org.cxct.sportlottery.util

import android.graphics.Rect
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * @author kevin
 * @create 2022/3/31
 * @description
 */
fun RecyclerView.addScrollWithItemVisibility(onVisible: (visibleList: List<Int>) -> Unit) {

    addOnScrollListener(object : RecyclerView.OnScrollListener() {

        private fun emitVisibleItems() {
            val manager = layoutManager
            if (manager is LinearLayoutManager) {
                val firstPosition = manager.findFirstVisibleItemPosition()
                val lastPosition = manager.findLastVisibleItemPosition()
                val visibleRange = mutableListOf<Int>()
                for (i in firstPosition..lastPosition) {
                    val view = manager.findViewByPosition(i) ?: continue
                    val rect = Rect()
                    val isVisible = view.getGlobalVisibleRect(rect)
                    if (isVisible) {
                        visibleRange.add(i)
                    }
                }
                onVisible(visibleRange)
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                emitVisibleItems()
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dx == 0 && dy == 0) {
                emitVisibleItems()
            }
        }

    })

}