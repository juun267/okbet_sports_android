package org.cxct.sportlottery.network.matchLiveInfo


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatLiveLoginRequest(
    @Json(name = "userName")
    val userName: String?,
    @Json(name = "platUserId")
    val platUserId: Long?,
    @Json(name = "iconUrl")
    val iconUrl: String?,
    @Json(name = "birthday")
    val birthday: String?,
    @Json(name = "nickName")
    val nickName: String?,
    @Json(name = "loginSrc")
    val loginSrc: Int?,
    @Json(name = "sign")
    val sign: String?,
    @Json(name = "timestamp")
    val timestamp: Long?,

    )