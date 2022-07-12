package org.cxct.sportlottery.network.index.register

data class RegisterRequest(
    val userName: String,
    val password: String, //密码，md5(明文)，校验格式：32位md5值
    val loginSrc: Long, //登录来源（0：WEB, 1：MOBILE_BROWSER, 2：ANDROID, 3：IOS）
    val deviceSn: String, //设备号（手机app登录必传）
    var fullName: String? = null,
    var email: String? = null,
    var address: String? = null,
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
    var telegram: String? = null,
    var province: String? = null,
    var city: String? = null,
    var zipCode: String? = null,
    var safeQuestionType: String? = null,
    var safeQuestion: String? = null,
    var loginEnvInfo:String? = null,
    var birthday: String? = null,
    var identityNumber: String? = null,
    var verifyPhoto1: String? = null,
    var verifyPhoto2: String? = null,
    var identityType: String? = null,
    var bettingStationId: String? = null,
    var salarySource: String? = null,
)
