package org.cxct.sportlottery.network.matchLiveInfo


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatLiveLoginData(
    @Json(name = "liveToken")
    val liveToken: String?,
    @Json(name = "money")
    val money: String?,
    @Json(name = "platformId")
    val platformId: Int?,
    @Json(name = "userData")
    val userData: ChatLiveUserInfo?,
)