package org.cxct.sportlottery.view.verticalMarquee

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager

class VerticalMarqueeView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : RecyclerView(context, attrs, defStyle) {

    private val handler = Handler(Looper.getMainLooper())
    private var scrollPosition = 0
    var delayMillis: Long = 3000
    override fun onTouchEvent(e: MotionEvent?): Boolean {
        return false
    }

    //判断是否拦截事件
    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        return true
    }
    fun setUp(adapter: BindingAdapter<*,*>){
        // 设置Adapter
        this.adapter = adapter
        // 设置LinearLayoutManager为垂直滚动
        layoutManager = SlowLinearLayoutManager(context, VERTICAL, false)

        // 自动滚动的逻辑
        startAutoScroll()
    }

    private fun startAutoScroll() {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed(object : Runnable {
            override fun run() {
                scrollPosition++
                smoothScrollToPosition(scrollPosition) // 平滑滚动到下一位置
                handler.postDelayed(this, delayMillis) // 每2秒滚动一次
            }
        }, delayMillis)
    }


    fun startAuto() {
        if (adapter?.itemCount == null || adapter?.itemCount == 0 )
            return
        startAutoScroll()
    }

    fun stopAuto() {
        handler.removeCallbacksAndMessages(null) // 防止内存泄漏
    }

    override fun onDetachedFromWindow() {
        stopAuto()
        super.onDetachedFromWindow()
    }

    fun bindLifecycler(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_RESUME) {
                    startAuto()
                } else if (event == Lifecycle.Event.ON_STOP) {
                    stopAuto()
                }
            }
        })
    }

}