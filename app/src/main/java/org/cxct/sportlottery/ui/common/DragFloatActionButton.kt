package org.cxct.sportlottery.ui.common

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import kotlin.math.sqrt


const val DURATION: Long = 300
const val DEFAULT_POSITION_X = 16f


class DragFloatActionButton : LinearLayout {


    private var parentHeight: Int = 0
    private var parentWidth: Int = 0


    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    private var lastX: Int = 0
    private var lastY: Int = 0


    private var isDrag: Boolean = false


    var actionUpListener: ActionUpListener? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val rawX = event!!.rawX.toInt()
        val rawY = event.rawY.toInt()
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                isDrag = false
                parent.requestDisallowInterceptTouchEvent(true)
                lastX = rawX
                lastY = rawY
                val parent: ViewGroup
                if (getParent() != null) {
                    parent = getParent() as ViewGroup
                    parentHeight = parent.height
                    parentWidth = parent.width
                }
            }
            MotionEvent.ACTION_MOVE -> {
                isDrag = !(parentHeight <= 0 || parentWidth == 0)
                val dx = rawX - lastX
                val dy = rawY - lastY
                val distance = sqrt((dx * dx + dy * dy).toDouble()).toInt()
                if (distance == 0) {
                    isDrag = false
                } else {
                    var x = x + dx
                    var y = y + dy
                    x = if (x < 0) 0F else if (x > parentWidth - width) (parentWidth - width).toFloat() else x
                    y = if (getY() < 0) 0F else if (getY() + height > parentHeight) (parentHeight - height).toFloat() else y
                    setX(x)
                    setY(y)
                    lastX = rawX
                    lastY = rawY

                }
            }
            MotionEvent.ACTION_UP -> if (!isNotDrag()) {
                isPressed = false
                if (rawX >= parentWidth / 2) {
                    animate().setInterpolator(DecelerateInterpolator())
                        .setDuration(DURATION)
                        .xBy(parentWidth - width - x - DEFAULT_POSITION_X)
                        .setListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator?) {
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                actionUpListener?.getPosition()
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                            override fun onAnimationRepeat(animation: Animator?) {
                            }
                        })
                        .start()
                } else {
                    val oa = ObjectAnimator.ofFloat(this, "x", x, DEFAULT_POSITION_X)
                    oa.interpolator = DecelerateInterpolator()
                    oa.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator?) {
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            actionUpListener?.getPosition()
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                        }

                        override fun onAnimationRepeat(animation: Animator?) {
                        }
                    })
                    oa.duration = DURATION
                    oa.start()
                }
            }
        }
        return !isNotDrag() || super.onTouchEvent(event)
    }


    private fun isNotDrag(): Boolean {
        return !isDrag && (x == DEFAULT_POSITION_X || x == (parentWidth - width - DEFAULT_POSITION_X))
    }


    class ActionUpListener(private val actionUp: () -> Unit) {
        fun getPosition() = actionUp()
    }


}