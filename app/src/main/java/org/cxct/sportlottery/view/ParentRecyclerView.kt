package org.cxct.sportlottery.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class ParentRecyclerView  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle)  {

    //return false 不拦截，继续分发下去
    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        return false
    }
}