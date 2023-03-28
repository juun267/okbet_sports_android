package org.cxct.sportlottery.common.extentions

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlinx.coroutines.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.cxct.sportlottery.network.common.BaseResult
import retrofit2.Response


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

inline fun Activity.finishWithOK() {
    setResult(Activity.RESULT_OK)
    finish()
}

inline fun Fragment.startActivity(activity: Class<out Activity>) {
    startActivity(Intent(requireActivity(), activity))
}

inline fun Activity.startActivity(activity: Class<out Activity>) {
    startActivity(Intent(this, activity))
}

fun Activity.bindFinish(vararg views: View) {
    views.forEach { it.setOnClickListener { finish() } }
}
