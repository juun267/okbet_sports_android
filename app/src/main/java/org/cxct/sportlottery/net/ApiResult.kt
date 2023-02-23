package org.cxct.sportlottery.net

import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.LocalUtils

open class ApiResult<T>() {

    private constructor(code: Int, msg: String, success: Boolean): this() {
        this.code = code
        this.msg = msg
        this.success = success
    }

    var code: Int = -1
        private set
        get() {
            return field ?: -1
        }

    var msg: String = ""
        private set
        get() {
            return field ?: ""
        }

    private var success: Boolean = false
        get() {
            return field ?: false
        }

    private val t: T? = null

    // 可以重新改方法，解析不同字段的data
    open fun getData(): T? = t
    open fun succeeded(): Boolean = success

    companion object {
        private const val ERROR_CODE_UNKNOWN = -1
        private const val ERROR_CODE_NET = -1000

        fun <T> unknownError(): ApiResult<T> {
            return ApiResult(ERROR_CODE_UNKNOWN, LocalUtils.getString(R.string.unknown_error), false)
        }

        fun <T> netError(): ApiResult<T> {
            return ApiResult(ERROR_CODE_NET, LocalUtils.getString(R.string.error), false)
        }
    }

}