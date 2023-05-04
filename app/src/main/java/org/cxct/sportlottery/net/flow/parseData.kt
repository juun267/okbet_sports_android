package org.cxct.sportlottery.net.flow

import org.cxct.sportlottery.net.flow.entity.ApiEmptyResponse
import org.cxct.sportlottery.net.flow.entity.ApiErrorResponse
import org.cxct.sportlottery.net.flow.entity.ApiFailedResponse
import org.cxct.sportlottery.net.flow.entity.ApiSuccessResponse
import org.cxct.sportlottery.common.extentions.toast
import org.cxct.sportlottery.net.ApiResult

fun <T> ApiResult<T>.parseData(listenerBuilder: ResultBuilder<T>.() -> Unit) {
    val listener = ResultBuilder<T>().also(listenerBuilder)
    when (this) {
        is ApiSuccessResponse -> listener.onSuccess(this.response)
        is ApiEmptyResponse -> listener.onDataEmpty()
        is ApiFailedResponse -> listener.onFailed(this.code, this.msg)
        is ApiErrorResponse -> listener.onError(this.throwable)
    }
    listener.onComplete()
}

class ResultBuilder<T> {
    var onSuccess: (data: T?) -> Unit = {}
    var onDataEmpty: () -> Unit = {}
    var onFailed: (errorCode: Int?, errorMsg: String?) -> Unit = { _, errorMsg ->
        errorMsg?.let { toast(it) }
    }
    var onError: (e: Throwable) -> Unit = { e ->
        e.message?.let { toast(it) }
    }
    var onComplete: () -> Unit = {}
}