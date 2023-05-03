package org.cxct.sportlottery.net.flow

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.cxct.sportlottery.net.ApiResult

fun <T> launchFlow(
    requestBlock: suspend () -> ApiResult<T>,
    startCallback: (() -> Unit)? = null,
    completeCallback: (() -> Unit)? = null,
): Flow<ApiResult<T>> {
    return flow {
        emit(requestBlock())
    }.onStart {
        startCallback?.invoke()
    }.onCompletion {
        completeCallback?.invoke()
    }
}

/**
 * 这个方法只是简单的一个封装Loading的普通方法，不返回任何实体类
 */
fun IUiView.launchWithLoading(requestBlock: suspend () -> Unit) {
    lifecycleScope.launch {
        flow {
            emit(requestBlock())
        }.onStart {
            showLoading()
        }.onCompletion {
            dismissLoading()
        }.collect()
    }
}

/**
 * 请求不带Loading&&不需要声明LiveData
 */
fun <T> IUiView.launchAndCollect(
    requestBlock: suspend () -> ApiResult<T>,
    listenerBuilder: ResultBuilder<T>.() -> Unit,
) {
    lifecycleScope.launch {
        launchFlow(requestBlock).collect { response ->
            response.parseData(listenerBuilder)
        }
    }
}

/**
 * 请求带Loading&&不需要声明LiveData
 */
fun <T> IUiView.launchWithLoadingAndCollect(
    requestBlock: suspend () -> ApiResult<T>,
    listenerBuilder: ResultBuilder<T>.() -> Unit,
) {
    lifecycleScope.launch {
        launchFlow(requestBlock, { showLoading() }, { dismissLoading() }).collect { response ->
            response.parseData(listenerBuilder)
        }
    }
}

//fun <T> Flow<ApiResult<T>>.collectIn(
//    lifecycleOwner: LifecycleOwner,
//    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
//    listenerBuilder: ResultBuilder<T>.() -> Unit,
//): Job = lifecycleOwner.lifecycleScope.launch {
//    flowWithLifecycle(lifecycleOwner.lifecycle,
//        minActiveState).collect { ApiResult: ApiResult<T> ->
//        ApiResult.parseData(listenerBuilder)
//    }
//}



