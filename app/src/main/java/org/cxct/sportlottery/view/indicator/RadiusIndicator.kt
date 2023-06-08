package org.cxct.sportlottery.view.indicator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

/**
 * 援交矩形指示器
 */
class RadiusIndicator  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr){


    private var mSpacing = 0
    private var mCurrentIndex = 0
    private var itemCount = 0
    private val itemPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mCirclePoints: MutableList<RectF> = ArrayList()
    private var mIndicatorX = 0f

    // 事件回调
    var isTouchable = false
    var itemClickListener: ((Int) -> Unit)? = null
    set(value) {
        if (!isTouchable) {
            isTouchable = true
        }
        field = value
    }
    private var mDownX = 0f
    private var mDownY = 0f
    private var mTouchSlop = 0
    private var isFollowTouch = true // 是否跟随手指滑动
    var mRadius = 0
    var itemWidth = 0
    var itemHeight = 0

    init {
        init(context)
    }

    private fun init(context: Context) {
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        itemPaint.style = Paint.Style.FILL
        indicatorPaint.style = Paint.Style.FILL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec))
    }

    private fun measureWidth(widthMeasureSpec: Int): Int {
        val mode = MeasureSpec.getMode(widthMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        return when (mode) {
            MeasureSpec.EXACTLY -> width
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                itemCount * itemWidth + (itemCount - 1) * mSpacing + paddingLeft + paddingRight
            }
            else -> 0
        }
    }

    private fun measureHeight(heightMeasureSpec: Int): Int {
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        var result = 0
        when (mode) {
            MeasureSpec.EXACTLY -> result = height
            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> result =
                itemHeight + paddingTop + paddingBottom
            else -> {}
        }
        return result
    }

    override fun onDraw(canvas: Canvas) {
        drawItems(canvas)
        drawIndicator(canvas)
    }

    private fun drawItems(canvas: Canvas) {
        var i = 0
        val j = mCirclePoints.size
        while (i < j) {
            val pointF = mCirclePoints[i]
            canvas.drawRoundRect(pointF, 4f, 4f, itemPaint)
            i++
        }
    }

    private fun drawIndicator(canvas: Canvas) {
        if (mCirclePoints.size > 0) {
            canvas.drawRoundRect(
                RectF(
                    mIndicatorX,
                    paddingTop.toFloat(),
                    mIndicatorX + itemWidth,
                    itemHeight.toFloat() + paddingTop
                ), mRadius.toFloat(), mRadius.toFloat(), indicatorPaint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (itemClickListener == null) {
            return super.onTouchEvent(event)
        }

        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> if (isTouchable) {
                mDownX = x
                mDownY = y
                return true
            }
            MotionEvent.ACTION_UP ->  {
                if (Math.abs(x - mDownX) <= mTouchSlop && Math.abs(y - mDownY) <= mTouchSlop) {
                    var max = Float.MAX_VALUE
                    var index = 0
                    var i = 0
                    while (i < mCirclePoints.size) {
                        val rectF = mCirclePoints[i]
                        val offset = Math.abs(rectF.right - x)
                        if (offset < max) {
                            max = offset
                            index = i
                        }
                        i++
                    }
                    itemClickListener!!.invoke(index)
                }
            }
            else -> {}
        }
        return super.onTouchEvent(event)
    }

    private fun prepareCirclePoints() {
        mCirclePoints.clear()
        if (itemCount == 0) {
            return
        }

        val top = paddingTop.toFloat()
        val bottom = top + itemHeight
        for (i in 0 until itemCount) {
            val left = paddingLeft + (itemWidth + mSpacing) * i
            mCirclePoints.add(RectF(left.toFloat(), top, (left + itemWidth).toFloat(), bottom))
        }
        mIndicatorX = mCirclePoints[mCurrentIndex].left
    }

    fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (!isFollowTouch || mCirclePoints.isEmpty()) {
            return
        }

        val currentPosition = Math.min(mCirclePoints.size - 1, position)
        val nextPosition = Math.min(mCirclePoints.size - 1, position + 1)
        val current = mCirclePoints[currentPosition]
        val next = mCirclePoints[nextPosition]
        mIndicatorX = current.left + (next.left - current.left) * positionOffset
        invalidate()
    }

    fun onPageSelected(position: Int) {
        mCurrentIndex = position
        if (!isFollowTouch) {
            mIndicatorX = mCirclePoints[mCurrentIndex].left
            invalidate()
        }
    }

    fun onPageScrollStateChanged(state: Int) {}
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        prepareCirclePoints()
    }

    fun notifyDataSetChanged() {
        prepareCirclePoints()
        requestLayout()
    }

    fun setIndicatorColor(normal: Int, selected: Int) {
        itemPaint.color = normal
        indicatorPaint.color = selected
    }

    fun setSpacing(circleSpacing: Int) {
        mSpacing = circleSpacing
        prepareCirclePoints()
    }

    fun resetItemCount(count: Int) {
        if (itemCount != count) {
            itemCount = count
            prepareCirclePoints()
            requestLayout()
        }
    }
}