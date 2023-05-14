package org.cxct.sportlottery.util

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
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
            override fun onAnimationStart(animation: Animator?) {
                onAnimStartListener?.invoke()
            }

            override fun onAnimationEnd(animation: Animator?) {
                onAnimEndListener?.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
        })
        return translationObjectAnimator
    }


}