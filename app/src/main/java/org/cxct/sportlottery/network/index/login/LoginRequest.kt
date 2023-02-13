package org.cxct.sportlottery.network.index.login


data class LoginRequest(
    val account: String,
    var password: String? = null, //MD5 加密
    val loginSrc: Long, //登录来源（0：WEB, 1：MOBILE_BROWSER, 2：ANDROID, 3：IOS）
    val deviceSn: String, //设备号（手机app登录必传）
    var validCodeIdentity: String? = null,
    var validCode: String? = null,
    var appVersion: String? = null,
    var loginEnvInfo: String? = null, //登入环境信息
    var SecurityCode: String? = null, //短信或者邮件验证码
)
