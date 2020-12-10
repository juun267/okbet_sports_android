package org.cxct.sportlottery.network.index


data class LoginRequest(
    val account: String,
    val password: String,
    val loginSrc: Int = 2, //login source android
    val validCodeIdentity: String? = null,
    val validCode: String? = null,
    val deviceSn: String? = null,
    val appVersion: String? = null
)
