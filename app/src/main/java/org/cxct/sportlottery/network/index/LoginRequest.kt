package org.cxct.sportlottery.network.index


data class LoginRequest(
    val account: String,
    val password: String, //MD5 加密
    val loginSrc: Int = 2, // 登录来源（0：WEB, 1：MOBILE_BROWSER, 2：ANDROID, 3：IOS）
    val validCodeIdentity: String? = null,
    val validCode: String? = null,
    val deviceSn: String? = null,
    val appVersion: String? = null
)
