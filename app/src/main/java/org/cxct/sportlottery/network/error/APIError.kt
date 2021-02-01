package org.cxct.sportlottery.network.error


data class APIError(
    val code: Int? = null,
    val msg: String? = null,
    val success: Boolean? = null,
)
