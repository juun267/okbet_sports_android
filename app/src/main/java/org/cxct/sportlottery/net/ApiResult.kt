package org.cxct.sportlottery.net

import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication

open class ApiResult<T>(): java.io.Serializable {

    private constructor(code: Int = -1, msg: String = "", success: Boolean = false): this() {
        this.code = code
        this.msg = msg
        this.success = success
    }

    var code: Int = -1
        protected set

    var msg: String = ""
        protected set
        get() {
            return field ?: ""
        }

    var success: Boolean = false
        protected set

    private val t: T? = null

    val total: Int = 0
    //当前页数，返回数据传值，方便做分页功能
    var page: Int? = null
    private val rows: T? = null

    // 可以重新改方法，解析不同字段的data
    open fun getData(): T? = t ?: rows
    open fun succeeded(): Boolean = success

    companion object {

        private const val ERROR_CODE_UNKNOWN = -1
        private const val ERROR_CODE_NET = -1000

        fun <A: ApiResult<*>> unknownError(apiResult: A): A {
            apiResult.code = ERROR_CODE_UNKNOWN
            apiResult.msg = MultiLanguagesApplication.stringOf(R.string.unknown_error)
            apiResult.success = false
            return apiResult
        }

        fun <A: ApiResult<*>> netError(apiResult: A): A {
            apiResult.code = ERROR_CODE_NET
            apiResult.msg = MultiLanguagesApplication.stringOf(R.string.N655)
            apiResult.success = false
            return apiResult
        }
    }

}
