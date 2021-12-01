package org.cxct.sportlottery.network.index.login_for_guest

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginForGuestRequest(
    @Json(name = "loginSrc")
    val loginSrc: Int ?= 2, //登录来源（0：WEB, 1：MOBILE_BROWSER, 2：ANDROID, 3：IOS)
    @Json(name = "deviceSn")
    val deviceSn: String, //设备号（手机app登录必传）
)