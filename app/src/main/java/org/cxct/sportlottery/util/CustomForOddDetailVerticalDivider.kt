package org.cxct.sportlottery.util


import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView


/**
 * @author Kevin
 * @create 2021/06/22
 * @description 直單行列表平均分佈item
 */
class CustomForOddDetailVerticalDivider(private val context: Context, private val dimenRes: Int) :
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
        if (parent.getChildAdapterPosition(view) == parent.adapter?.itemCount?.minus(1) ?: 0) {
            outRect.bottom = space
        }
        outRect.top = space
    }


}