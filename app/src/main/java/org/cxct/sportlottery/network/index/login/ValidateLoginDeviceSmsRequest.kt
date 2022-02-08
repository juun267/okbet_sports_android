package org.cxct.sportlottery.network.index.login


data class ValidateLoginDeviceSmsRequest(
    val loginEnvInfo: String? = null,
    val validCode: String? = null,
    val loginSrc: Long
)
