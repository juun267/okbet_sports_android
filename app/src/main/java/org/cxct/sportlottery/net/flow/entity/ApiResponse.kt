package org.cxct.sportlottery.net.flow.entity

import org.cxct.sportlottery.net.ApiResult
import java.io.Serializable

//open class ApiResponse<T>(
//        open val data: T? = null,
//        open val errorCode: Int? = null,
//        open val errorMsg: String? = null,
//        open val error: Throwable? = null,
//) : Serializable {
//    val isSuccess: Boolean
//        get() = errorCode == 0
//
//    override fun toString(): String {
//        return "ApiResponse(data=$data, errorCode=$errorCode, errorMsg=$errorMsg, error=$error)"
//    }
//
//
//}

data class ApiSuccessResponse<T>(val response: T) : ApiResult<T>()

class ApiEmptyResponse<T> : ApiResult<T>()

data class ApiFailedResponse<T>(val codes: Int?,  val msgs: String?) : ApiResult<T>(code = codes, msg = msgs)

data class ApiErrorResponse<T>(val throwable: Throwable) : ApiResult<T>()
