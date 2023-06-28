package org.cxct.sportlottery.view

import android.text.Editable
import android.view.View
import android.widget.EditText

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


fun View.isVisible():Boolean{
    return visibility==View.VISIBLE
}

fun EditText.insertEmoji(emoji:String){
    val index=selectionStart
    val text:Editable=text
    text.insert(index,emoji)
}