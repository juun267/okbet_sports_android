package org.cxct.sportlottery.network.index.register

data class RegisterRequest(
    val userName: String? = null,
    val password: String? = null, //密码，md5(明文)，校验格式：32位md5值
    val loginSrc: Long? = null, //登录来源（0：WEB, 1：MOBILE_BROWSER, 2：ANDROID, 3：IOS）
    val deviceSn: String? = null,
    var fullName: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var qq: String? = null,
    var fundPwd: String? = null,
    var validCodeIdentity: String? = null,
    var validCode: String? = null,
    var wechat: String? = null,
    var securityCode: String? = null,
    var inviteCode: String? = null,
    var zalo: String? = null,
    var facebook: String? = null,
    var whatsapp: String? = null,
    var telegram: String? = null
)
