package org.cxct.sportlottery.ui.chat

open class BaseOutput {
    var success: Boolean? = null //是否成功（true：成功，false：失败）

    var msg: String? = null //返回的信息描述

    var code: Int? = null //返回代码

    fun getErrorOutput(): ErrorOutput {
        val error = ErrorOutput()
        error.code = code
        error.msg = msg
        return error
    }
}