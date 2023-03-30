package org.cxct.sportlottery.extentions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.net.ApiResult

import java.io.IOException

fun LifecycleOwner.launch(block: suspend () -> Unit ) = lifecycleScope.launch { block() }

suspend fun <T> safeApi(block: suspend() -> ApiResult<T>): ApiResult<T> {

    return try {
        block()
    } catch (e: Exception) {
        e.printStackTrace()
        if (e is IOException) {
            return ApiResult.netError()
        }
        return ApiResult.unknownError()
    }
}

fun <T> LifecycleOwner.callApi(apiCall: suspend() -> ApiResult<T>, block: (ApiResult<T>) -> Unit)  = lifecycleScope.callApi(apiCall, block)

fun <T> CoroutineScope.callApi(apiCall: suspend() -> ApiResult<T>, block: (ApiResult<T>) -> Unit) {
    launch(Dispatchers.IO) {
        val result = safeApi(apiCall)
        launch(Dispatchers.Main) { block(result) }
    }
}

fun <T> callApiWithNoCancel(apiCall: suspend() -> ApiResult<T>, block: (ApiResult<T>) -> Unit) {
    GlobalScope.launch(Dispatchers.IO) {
        val result = safeApi(apiCall)
        GlobalScope.launch(Dispatchers.Main) { block(result) }
    }
}

fun <T> ViewModel.callApi(apiCall: suspend() -> ApiResult<T>, block: (ApiResult<T>) -> Unit) = viewModelScope.callApi(apiCall, block)