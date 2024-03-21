package org.cxct.sportlottery.view

import android.content.Context
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.MetricsUtil.convertDpToPixel
import timber.log.Timber
import java.util.*

/**
 * 自動滑動 RecycleView
 *
 * 使用方法:
 * 在 onResume() 呼叫 startAuto()
 * 在 onPause() 呼叫 stopAuto()
 *
 * created by Simon Chang
 *
 *
 * 設定跑馬燈速度  app:speed_sec_dp="50dp" (每秒移動 50dp)
 */
class AutoScrollRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : RecyclerView(context, attrs, defStyle) {
    private var speedPxSec = 0
    private var mTimer: Timer? = null
    private var isEmpty: Boolean = true

    init {
        initData(attrs)
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                //判斷是否滑到最右邊，在設置到開頭以達到輪播效果
                if (!recyclerView.canScrollHorizontally(1))
                    scrollToPosition(0)
            }
        })
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        return false
    }

    //判断是否拦截事件
    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        return true
    }

    private fun initData(attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.AutoScrollRecyclerView, 0, 0)
        speedPxSec = try {
            //預設移動 50dp/sec 的速度
            val defaultPx = convertDpToPixel(50f, context).toInt()
            typedArray.getDimensionPixelSize(R.styleable.AutoScrollRecyclerView_speed_dp_sec, defaultPx)
        } finally {
            typedArray.recycle()
        }
    }

    fun startAuto(isEmpty: Boolean?) {
        if (isEmpty != null) this.isEmpty = isEmpty
        startAuto()
    }

    fun startAuto() {
        stopAuto()

        if (adapter?.itemCount == null || adapter?.itemCount == 0 || isEmpty)
            return
        //每 250L 毫秒執行一次 smoothScrollBy()
        //紀錄: smoothScrollBy() 的時間間隔為 250L，所以 period 設 250L 達到平順滑動的感覺
        val delay = 0
        val period = 250
        val distance = speedPxSec / 4 //因為動畫 250L 一次，所以除以 4 得到每次移動距離
        val interpolator: Interpolator = LinearInterpolator()
        mTimer = Timer()
        mTimer?.schedule(object : TimerTask() {
            override fun run() {
                //firebase fixbug:Fatal Exception: java.lang.IllegalStateException: Not in applications main thread
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    post {
                        smoothScrollBy(distance, distance, interpolator)
                    }
                }else{
                    smoothScrollBy(distance, distance, interpolator)
                }

            }
        }, delay.toLong(), period.toLong()) //在 0 秒後，每隔 250L 毫秒執行一次
    }

    fun stopAuto(isEmpty: Boolean?) {
        if (isEmpty != null) this.isEmpty = isEmpty
        stopAuto()
    }

    fun stopAuto() {
        mTimer?.cancel()
        mTimer = null
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