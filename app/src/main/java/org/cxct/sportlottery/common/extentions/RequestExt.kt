package org.cxct.sportlottery.common.extentions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import org.cxct.sportlottery.net.ApiResult

import java.io.IOException


inline fun <T> Flow<T>.collectWith(scope: CoroutineScope, collector: FlowCollector<T>) {
    scope.launch { collect(collector) }
}

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

fun <T> CoroutineScope.asyncApi(block: suspend() -> ApiResult<T>) = async { safeApi { block() } }
