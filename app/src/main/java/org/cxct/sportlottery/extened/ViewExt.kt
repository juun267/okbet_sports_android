package org.cxct.sportlottery.extened

import android.content.Context
import android.view.View

/**
 * 关于View的一些扩展函数
 */

fun View.visible(){
    this.visibility = View.VISIBLE
}

fun View.gone(){
    this.visibility = View.VISIBLE
}

fun View.inVisible(){
    this.visibility = View.INVISIBLE
}

//私有扩展属性，允许2次点击的间隔时间
private var <T : View> T.delayTime: Long
    get() = getTag(0x7FFF0001) as? Long ?: 0
    set(value) {
        setTag(0x7FFF0001, value)
    }

//私有扩展属性，记录点击时的时间戳
private var <T : View> T.lastClickTime: Long
    get() = getTag(0x7FFF0002) as? Long ?: 0
    set(value) {
        setTag(0x7FFF0002, value)
    }

//私有扩展方法，判断能否触发点击事件
private fun <T : View> T.canClick(): Boolean {
    var flag = false
    var now = System.currentTimeMillis()
    if (now - this.lastClickTime >= this.delayTime) {
        flag = true
        this.lastClickTime = now
    }
    return flag
}

//扩展点击事件，默认 300ms 内不能触发 2 次点击
fun <T : View> T.clickWithDuration(time: Long = 300, block: (T) -> Unit) {
    delayTime = time
    setOnClickListener {
        if (canClick()) {
            block(this)
        }
    }
}