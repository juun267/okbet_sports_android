package org.cxct.sportlottery.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * CHATGPT生成的代码
 */
class FollowBallView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private var xPosition = 0f
    private var yPosition= 0f

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawCircle(x, y, 50f, paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        xPosition = event?.x ?: 0f
        yPosition = event?.y ?: 0f
        invalidate()
        return true
    }
}
