package org.cxct.sportlottery.network.common


abstract class BaseResult {
    abstract val code: Int
    abstract val msg: String
    abstract val success: Boolean

    companion object {
        const val errorNetCode = 1000
        const val errorNetResult = "{\"code\":-1, \"msg\":\"Invalid network\", \"success\":false}"
    }
}