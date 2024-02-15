package org.cxct.sportlottery.ui.sport.endscore

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.util.DisplayUtil.dp

class EndScoreItemDecoration(val isSportDetail: Boolean = false) : RecyclerView.ItemDecoration() {

    private val spanCount = 5
    private val horSpacing = 6.dp
    private val verSpacing = 4.dp
    private val sideSpacing = 12.dp
    private val mDivider = ColorDrawable(Color.parseColor("#336C7BA8"))
    private val lineHeight = 0.6f.dp
    private val divideHeight = 24.dp

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val odd = parent.getItemOddOrNull(view)
        if (odd==null) {
            super.getItemOffsets(outRect, view, parent, state)
        }else {
            //如果是BaseNode说明是末位比分大厅的数据
            val position = if (!isSportDetail){
                odd?.parentNode?.childNode?.indexOf(odd) ?: -1
            } else{
                parent.getChildAdapterPosition(view)
            }
            var bottomMargin = if (parent.needSetDivide(view)) verSpacing + divideHeight else verSpacing
            when (position % spanCount) {
                0 -> {
                    outRect.set(sideSpacing, verSpacing, 0, bottomMargin)
                }
                spanCount - 1 -> {
                    outRect.set(horSpacing, verSpacing, sideSpacing, bottomMargin)
                }
                else -> {
                    outRect.set(horSpacing, verSpacing, 0, bottomMargin)
                }
            }
        }
    }
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            if (parent.needSetDivide(child)){
                val left = 20.dp
                val right = c.width - 20.dp
                val top = child.bottom +(verSpacing+divideHeight+lineHeight)/2
                val bottom = top + lineHeight
                mDivider.setBounds(left, top, right, bottom)
                mDivider.draw(c)
            }else{
                super.onDraw(c, parent, state)
            }
        }
    }

    private fun RecyclerView.getItemOddOrNull(childView: View): Odd?{
    val position = getChildAdapterPosition(childView)
    val adapter=adapter as BaseQuickAdapter<*, *>
    return adapter.getItemOrNull(position) as? Odd
    }
    /**
     * 是否需要设置分割线
     */
    private fun RecyclerView.needSetDivide(childView: View): Boolean{
        return if (isSportDetail) {
            val position = getChildAdapterPosition(childView)
            (position / spanCount) % 2 == 1 && position < ((adapter?.itemCount?:-1) - spanCount)
        }else{
            val odd = getItemOddOrNull(childView)
            val indexInParent = odd?.parentNode?.childNode?.indexOf(odd)?:-1
            (indexInParent / spanCount) % 2 == 1 && indexInParent < (odd?.parentNode?.childNode!!.size - spanCount-1)
        }
    }
}