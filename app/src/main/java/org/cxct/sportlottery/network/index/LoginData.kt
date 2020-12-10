package org.cxct.sportlottery.network.index

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class LoginData(
    @Json(name = "fullName")
    val fullName: String,
    @Json(name = "iconUrl")
    val iconUrl: String?,
    @Json(name = "lastLoginDate")
    val lastLoginDate: Long,
    @Json(name = "lastLoginIp")
    val lastLoginIp: String,
    @Json(name = "loginDate")
    val loginDate: Long,
    @Json(name = "loginIp")
    val loginIp: String,
    @Json(name = "nickName")
    val nickName: String?,
    @Json(name = "platformId")
    val platformId: Int,
    @Json(name = "rechLevel")
    val rechLevel: String?,
    @Json(name = "testFlag")
    val testFlag: Int,
    @Json(name = "token")
    val token: String,
    @Json(name = "uid")
    val uid: Int,
    @Json(name = "userId")
    val userId: Int,
    @Json(name = "userName")
    val userName: String,
    @Json(name = "userType")
    val userType: String
)