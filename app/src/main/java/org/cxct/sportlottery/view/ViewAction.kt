package org.cxct.sportlottery.view

import android.view.TextureView
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat

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