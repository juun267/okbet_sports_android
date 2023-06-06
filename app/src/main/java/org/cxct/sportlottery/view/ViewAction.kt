package org.cxct.sportlottery.view

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.util.Log
import android.view.TextureView
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.util.DisplayUtil.dp

object ViewAction {

}


private var lastClickTime = 0

/**
 * 防连续点击click
 */
fun View.onClick(block: () -> Unit) {
    val systemTime = System.currentTimeMillis()
    this.setOnClickListener {
        if (systemTime - lastClickTime < 200) {
            return@setOnClickListener
        }
        block()
    }
}


fun TextView.setColors(colorResource:Int){
    setTextColor(ContextCompat.getColor(this.context,colorResource))
}

fun View.expand(height:Int){
    val animator=ObjectAnimator.ofInt(height)
    animator.duration = 200
    animator.addUpdateListener {
        this.layoutParams.height=animator.animatedValue as Int
        this.requestLayout()
    }
    animator.start()
}

fun View.unExpand(height:Int){
    val animator=ObjectAnimator.ofInt(height,0)
    animator.duration = 200
    animator.addUpdateListener {
        this.layoutParams.height=animator.animatedValue as Int
        this.requestLayout()
    }
    animator.start()
}

