package org.cxct.sportlottery.view.floatingbtn

import android.animation.ObjectAnimator
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator

class SuckEdgeTouch(val autoSide: Boolean = true,
                    private val onSide: ((Boolean) -> Unit)? = null ,
                    private val onMove: ((Float) -> Unit)? = null): OnTouchListener {

    private val CLICK_DRAG_TOLERANCE = 10f // Often, there will be a slight, unintentional, drag when the user taps the FAB, so we need to account for this.
    private var downRawX = 0f
    private var downRawY = 0f
    private var dX = 0f
    private var dY = 0f

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {

        val action: Int = motionEvent.action
        return if (action == MotionEvent.ACTION_DOWN) {
            downRawX = motionEvent.rawX
            downRawY = motionEvent.rawY
            dX = view.x - downRawX
            dY = view.y - downRawY
            true // Consumed
        } else if (action == MotionEvent.ACTION_MOVE) {
            val viewWidth: Int = view.width
            val viewHeight: Int = view.height
            val viewParent = view.parent as View
            val parentWidth = viewParent.width
            val parentHeight = viewParent.height
            var newX = motionEvent.rawX + dX
            newX = Math.max(0f, newX) // Don't allow the FAB past the left hand side of the parent
            newX = Math.min((parentWidth - viewWidth).toFloat(), newX) // Don't allow the FAB past the right hand side of the parent
            var newY = motionEvent.rawY + dY
            newY = Math.max(0f, newY) // Don't allow the FAB past the top of the parent
            newY = Math.min((parentHeight - viewHeight).toFloat(), newY) // Don't allow the FAB past the bottom of the parent
            view.x = newX
            view.y = newY
            onMove?.invoke(newY)
            true // Consumed
        } else if (action == MotionEvent.ACTION_UP) {
            val upRawX = motionEvent.getRawX()
            val upRawY = motionEvent.getRawY()
            val parentWidth = (view.parent as ViewGroup).width.toFloat()
            val upDX = upRawX - downRawX
            val upDY = upRawY - downRawY
            if (autoSide) {
                val decelerateInterpolator = DecelerateInterpolator()
                if (upRawX >= parentWidth / 2) {
                    //靠右吸附
                    view.animate().setInterpolator(decelerateInterpolator)
                        .setDuration(500)
                        .xBy(parentWidth - view.width - view.x)
                        .start()
                    onSide?.invoke(false)
                } else {
                    //靠左吸附
                    val oa = ObjectAnimator.ofFloat(view, "x", view.getX(), 0f)
                    oa.interpolator = decelerateInterpolator
                    oa.duration = 500
                    oa.start()
                    onSide?.invoke(true)
                }
            }
            if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) { // A click
                view.performClick()
            } else { // A drag
                true // Consumed
            }
        } else {
            false
        }
    }
}