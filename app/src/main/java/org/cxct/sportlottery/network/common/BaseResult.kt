package org.cxct.sportlottery.network.common


abstract class BaseResult {
    abstract val code: Int
    abstract val msg: String
    abstract val success: Boolean

}