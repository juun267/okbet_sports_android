package org.cxct.sportlottery.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class ChildRecyclerView  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle)  {


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        //通知父层ViewGroup不要拦截点击事件
        parent.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(ev)
    }
}