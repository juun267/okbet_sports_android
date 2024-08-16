package org.cxct.sportlottery.util

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Path
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BaseInterpolator

object AnimatorUtils {

    private const val translationY: String = "translationY"

    /**
     * translationY 属性动画
     */
    fun startTranslationY(
        targetView: View,
        fromY: Int,
        toY: Int,
        interpolator: BaseInterpolator? = null,
        duration: Long = 400,
        isStart: Boolean = true,
        onAnimEndListener: (() -> Unit)? = null,
        onAnimStartListener: (() -> Unit)? = null
    ): ValueAnimator {
        val translationObjectAnimator = ObjectAnimator.ofFloat(
            targetView, translationY, fromY.toFloat(), toY.toFloat()
        )
        translationObjectAnimator.also {
            it.interpolator = interpolator
            it.duration = duration
            if (isStart) {
                it.start()
            }
        }.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                onAnimStartListener?.invoke()
            }

            override fun onAnimationEnd(animation: Animator) {
                onAnimEndListener?.invoke()
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
        return translationObjectAnimator
    }

    fun animateAddToCart(moveView: View, targetView: View) {
        // 获取 itemView 和 cartView 的坐标
        val itemViewCoords = IntArray(2)
        moveView.getLocationInWindow(itemViewCoords)
        val itemViewX = itemViewCoords[0]
        val itemViewY = itemViewCoords[1]

        val cartViewCoords = IntArray(2)
        targetView.getLocationInWindow(cartViewCoords)
        val cartViewX = cartViewCoords[0]
        val cartViewY = cartViewCoords[1]

        // 计算 itemView 的宽度和高度
        val itemViewWidth = moveView.width
        val itemViewHeight = moveView.height

        // 计算起点和终点
        val startX = itemViewX.toFloat()
        val startY = itemViewY.toFloat()
        val endX = cartViewX.toFloat() + targetView.width / 2 - itemViewWidth / 2
        val endY = cartViewY.toFloat() + targetView.height / 2 - itemViewHeight / 2

        // 创建动画路径
        val path = Path().apply {
            moveTo(startX, startY)
            quadTo(
                (startX + endX) / 2,
                startY - 300,  // 曲线的控制点，调整以获得所需的曲线效果
                endX,
                endY
            )
        }

        // 创建 ObjectAnimator
        val pathAnimator = ObjectAnimator.ofFloat(moveView, View.X, View.Y, path).apply {
            duration = 1000
            start()
        }
    }
}