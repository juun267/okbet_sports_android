package org.cxct.sportlottery.network.index.login_for_guest

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.LOGIN_SRC

@JsonClass(generateAdapter = true) @KeepMembers
data class LoginForGuestRequest(
    @Json(name = "loginSrc")
    val loginSrc: Long = LOGIN_SRC, //登录来源（0：WEB, 1：MOBILE_BROWSER, 2：ANDROID, 3：IOS)
    @Json(name = "deviceSn")
    val deviceSn: String = Constants.deviceSn,
)