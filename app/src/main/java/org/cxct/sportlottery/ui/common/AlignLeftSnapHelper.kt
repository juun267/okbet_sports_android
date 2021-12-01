package org.cxct.sportlottery.ui.common

import android.view.View
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * 使 RecycleView 滑動結束後 item 向左對齊
 * 使用方法 AlignLeftSnapHelper.attachToRecyclerView(recycleView);
 * https://blog.csdn.net/cygjay123/article/details/84998362
 */
class AlignLeftSnapHelper : LinearSnapHelper() {
    /**
     * 水平、垂直方向的度量
     */
    private var mVerticalHelper: OrientationHelper? = null
    private var mHorizontalHelper: OrientationHelper? = null

    var isLastItemAlignRight = false //滑動到最右側時，不對齊螢幕左側
    var isItemNeedOverScroll = false //滑動到最左右側時，須支援overScroll的情況下，避免item回彈時的位移

    private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (mVerticalHelper == null) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
        }
        return mVerticalHelper!!
    }

    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (mHorizontalHelper == null) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return mHorizontalHelper!!
    }

    /**
     * 计算出View对齐到指定位置，所需的x、y轴上的偏移量。
     */
    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray? {
        val out = IntArray(2)

        // 水平方向滑动时计算x方向，否则偏移为0
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToStart(layoutManager, targetView, getHorizontalHelper(layoutManager))
        } else {
            out[0] = 0
        }

        // 垂直方向滑动时计算y方向，否则偏移为0
        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToStart(layoutManager, targetView, getVerticalHelper(layoutManager))
        } else {
            out[1] = 0
        }
        return out
    }

    private fun distanceToStart(layoutManager: RecyclerView.LayoutManager, targetView: View, helper: OrientationHelper): Int {
        // RecyclerView的边界x值,也就是左侧Padding值
        val start: Int = if (layoutManager.clipToPadding) {
            helper.startAfterPadding
        } else {
            0
        }
        return helper.getDecoratedStart(targetView) - start
    }

    /**
     * 查找需要对齐的View
     */
    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        return if (layoutManager.canScrollVertically()) {
            findStartView(layoutManager, getVerticalHelper(layoutManager))
        } else {
            findStartView(layoutManager, getHorizontalHelper(layoutManager))
        }
    }

    private fun findStartView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper): View? {
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return null
        }
        var closestChild: View? = null
        val start: Int = if (layoutManager.clipToPadding) {
            helper.startAfterPadding
        } else {
            0
        }
        var absClosest = Int.MAX_VALUE
        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i)
            // ItemView 的左侧坐标
            val childStart = helper.getDecoratedStart(child)
            // 计算此ItemView 与 RecyclerView左侧的距离
            val absDistance = abs(childStart - start)

            // 找到那个最靠近左侧的ItemView然后返回


            if (absDistance < absClosest) {
                absClosest = absDistance
                closestChild = child
            }

            if (isItemNeedOverScroll) {
                if (i == 0) {
                    if (absDistance < 150) {
                        absClosest = 0
                        closestChild = layoutManager.getChildAt(0)
                    }
                }
            }

            if (isLastItemAlignRight) {
                if (childCount - 2 == 0) {
                    if (absDistance / 2 < absClosest) { //滑動到最右側倒數第二個item時，左半側對齊倒數第二個item，右半側對齊最後一個item
                        val lastChild = layoutManager.getChildAt(childCount - 1)
                        closestChild = lastChild
                    }
                }
            }

        }

        return closestChild
    }

    /**
     * 找到需要对齐的View的position，主要用于fling 操作
     */
    override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager, velocityX: Int, velocityY: Int): Int {
        // 左对齐和居中对齐一样，无需自定义处理
        return super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
    }
}