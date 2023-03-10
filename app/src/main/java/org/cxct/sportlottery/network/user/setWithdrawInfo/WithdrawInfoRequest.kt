package org.cxct.sportlottery.network.user.setWithdrawInfo


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class WithdrawInfoRequest(
    @Json(name = "email")
    var email: String? = null,
    @Json(name = "fullName")
    var fullName: String? = null,
    @Json(name = "fundPwd")
    var fundPwd: String? = null,
    @Json(name = "phone")
    var phone: String? = null,
    @Json(name = "qq")
    var qq: String? = null,
    @Json(name = "userId")
    val userId: Long,
    @Json(name = "wechat")
    var wechat: String? = null
)