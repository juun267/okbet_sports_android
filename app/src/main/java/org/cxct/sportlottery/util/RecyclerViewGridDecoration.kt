package org.cxct.sportlottery.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewGridDecoration(
    var spanCount: Int,
    var spacing: Int,
    val spacingTop: Int,
    val spacingBottom: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        var position = parent.getChildAdapterPosition(view)

        var column = position % spanCount
        outRect.left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
        outRect.right =
            spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)

        if(position<spanCount){
            outRect.top = spacingTop
        } else{
            outRect.top = spacing // item top
        }
        if(Math.floor(position.toDouble()/spanCount.toDouble()) == Math.floor((parent.adapter!!.itemCount-1).toDouble()/spanCount.toDouble())){
            outRect.bottom = spacingBottom
        }
    }


}
