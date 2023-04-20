package org.cxct.sportlottery.view

import android.view.View

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