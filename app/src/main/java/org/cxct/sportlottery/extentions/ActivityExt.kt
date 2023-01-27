package org.cxct.sportlottery.extentions

import androidx.annotation.MainThread
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

suspend fun <T : BaseResult> safeApi(block: suspend() -> Response<T>): Response<T> {
    return try {
        block()
    } catch (e: Exception) {
        e.printStackTrace()
        Response.error(BaseResult.errorNetCode, BaseResult.errorNetResult.toResponseBody())
    }
}

fun <T : BaseResult> LifecycleOwner.callApi(apiCall: suspend() -> Response<T>, block: (Response<T>) -> Unit)  = lifecycleScope.callApi(apiCall, block)

fun <T : BaseResult> CoroutineScope.callApi(apiCall: suspend() -> Response<T>, block: (Response<T>) -> Unit) {
    launch(Dispatchers.IO) {
        val result = safeApi(apiCall)
        launch(Dispatchers.Main) { block(result) }
    }
}

fun <T : BaseResult> callApiWithNoCancel(apiCall: suspend() -> Response<T>, block: (Response<T>) -> Unit) {
    GlobalScope.launch(Dispatchers.IO) {
        val result = safeApi(apiCall)
        GlobalScope.launch(Dispatchers.Main) { block(result) }
    }
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