package org.cxct.sportlottery.network.user.setWithdrawInfo


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WithdrawInfoRequest(
    @Json(name = "email")
    val email: String?,
    @Json(name = "fullName")
    val fullName: String?,
    @Json(name = "fundPwd")
    val fundPwd: String?,
    @Json(name = "phone")
    val phone: String?,
    @Json(name = "qq")
    val qq: String?,
    @Json(name = "userId")
    val userId: Int,
    @Json(name = "wechat")
    val wechat: String?
)