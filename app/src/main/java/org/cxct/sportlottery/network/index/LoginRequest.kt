package org.cxct.sportlottery.network.index


const val LOGIN_SOURCE_ANDROID = 2

data class LoginRequest(
    val account: String,
    val password: String,
    val loginSrc: Int = LOGIN_SOURCE_ANDROID,
    val validCodeIdentity: String? = null,
    val validCode: String? = null,
    val deviceSn: String? = null,
    val appVersion: String? = null
)
