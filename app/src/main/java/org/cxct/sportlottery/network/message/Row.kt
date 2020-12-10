package org.cxct.sportlottery.network.message


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Row(
    @Json(name = "addTime")
    val addTime: Long,
    @Json(name = "content")
    val content: String,
    @Json(name = "endTime")
    val endTime: Long,
    @Json(name = "id")
    val id: Int,
    @Json(name = "matchId")
    val matchId: String?,
    @Json(name = "messageType")
    val messageType: Int,
    @Json(name = "platformId")
    val platformId: Int,
    @Json(name = "startTime")
    val startTime: Long,
    @Json(name = "title")
    val title: String,
    @Json(name = "updateTime")
    val updateTime: Long,
    @Json(name = "url")
    val url: Int?
)