package org.cxct.sportlottery.network.message


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Row(
    @Json(name = "addTime")
    val addTime: String,
    @Json(name = "message")
    val message: String,
    @Json(name = "endTime")
    val endTime: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "matchId")
    val matchId: String?,
    @Json(name = "messageType")
    val messageType: Int,
    @Json(name = "platformId")
    val platformId: Int,
    @Json(name = "startTime")
    val startTime: String,
    @Json(name = "title")
    val title: String,
    @Json(name = "updateTime")
    val updateTime: String,
    @Json(name = "url")
    val url: Int?
)