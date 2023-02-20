package org.cxct.sportlottery.extentions

import androidx.annotation.MainThread
import androidx.lifecycle.*

// 防止LiveData数据倒灌
@MainThread
fun LiveData<*>.clean() {
    val versionField = LiveData::class.java.getDeclaredField("mVersion")
    versionField.isAccessible = true
    versionField.setInt(this, -1)
}

@MainThread
fun ViewModel.releaseVM() {
    val declaredField = ViewModel::class.java.getDeclaredField("mCleared")
    declaredField.isAccessible = true
    if (declaredField.getBoolean(this)) {
        return
    }

    val field = ViewModel::class.java.getDeclaredMethod("clear")
    field.isAccessible = true
    field.invoke(this)
}

fun LifecycleOwner.doOnResume(block: () -> Unit, interval: Int = 30_000) {
    doWhenLife(Lifecycle.Event.ON_RESUME, interval, block)
}

fun LifecycleOwner.doOnPause(block: () -> Unit) {
    doWhenLife(Lifecycle.Event.ON_RESUME, 0, block)
}

fun LifecycleOwner.doOnStop(block: () -> Unit) {
    doWhenLife(Lifecycle.Event.ON_RESUME, 0, block)
}

fun LifecycleOwner.doOnDestory(block: () -> Unit) {
    doWhenLife(Lifecycle.Event.ON_RESUME, 0, block)
}

fun LifecycleOwner.doWhenLife(lifeEvent: Lifecycle.Event, interval: Int = 0, block: () -> Unit, ) {
    lifecycle.addObserver(object : LifecycleEventObserver {

        var time = 0L
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == lifeEvent && System.currentTimeMillis() - time > interval) {
                time = System.currentTimeMillis()
                block.invoke()
            }
        }
    })
}