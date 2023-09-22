package org.cxct.sportlottery.view

import android.animation.Animator
import androidx.appcompat.widget.AppCompatTextView
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import org.cxct.sportlottery.common.extentions.runWithCatch

class StreamerTextView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(
    context!!, attrs, defStyleAttr) {
    private lateinit var mPaint: Paint
    private lateinit var mPath: Path
    private lateinit var mLinearGradient: LinearGradient
    private var mValueAnimator: ValueAnimator?=null
    private val runDelay = Runnable { mValueAnimator?.start()}

    init {
        init()
    }

    private fun init() {
        mPaint = Paint()
        mPath = Path()
    }

    private fun initPointAndAnimator(w: Int, h: Int) {
        val point1 = Point(0, 0)
        val point2 = Point(w, 0)
        val point3 = Point(w, h)
        val point4 = Point(0, h)
        mPath.moveTo(point1.x.toFloat(), point1.y.toFloat())
        mPath.lineTo(point2.x.toFloat(), point2.y.toFloat())
        mPath.lineTo(point3.x.toFloat(), point3.y.toFloat())
        mPath.lineTo(point4.x.toFloat(), point4.y.toFloat())
        mPath.close()

        // 斜率k
        val k = 1f * h / w
        // 偏移
        val offset = 1f * w / 2
        // 0f - offset * 2 为数值左边界（屏幕外左侧）， w + offset * 2为数值右边界（屏幕外右侧）
        // 目的是使光影走完一遍，加一些时间缓冲，不至于每次光影移动的间隔都那么急促
        mValueAnimator = ValueAnimator.ofFloat(0f - offset * 2, w + offset * 2).apply {
            interpolator = LinearInterpolator()
            duration = 1500
            addUpdateListener(AnimatorUpdateListener { animation ->
                val value = animation.animatedValue as Float
                mLinearGradient =
                    LinearGradient(value, k * value, value + offset, k * (value + offset), intArrayOf(
                        Color.parseColor("#00FFFFFF"),
                        Color.parseColor("#FFFFFFFF"),
                        Color.parseColor("#00FFFFFF")), null, Shader.TileMode.CLAMP)
                mPaint.shader = mLinearGradient
                invalidate()
            })
                addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    postDelayed(runDelay,3500)
                }
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }
        removeCallbacks(runDelay)
        mValueAnimator?.start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        initPointAndAnimator(widthSize, heightSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

//        会有异常
//        Fatal Exception: java.lang.IllegalArgumentException:
//        at android.graphics.LinearGradient.nativeCreate(LinearGradient.java)
//        at android.graphics.LinearGradient.createNativeInstance(LinearGradient.java:164)
//        at android.graphics.Shader.getNativeInstance(Shader.java:191)
//        at android.graphics.Paint.getNativeInstance(Paint.java:726)
//        at android.graphics.BaseRecordingCanvas.drawPath(BaseRecordingCanvas.java:547)
//        at org.cxct.sportlottery.view.StreamerTextView.onDraw(StreamerTextView.java:30)
        runWithCatch { canvas.drawPath(mPath, mPaint) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(runDelay)
        mValueAnimator?.cancel()
    }
}