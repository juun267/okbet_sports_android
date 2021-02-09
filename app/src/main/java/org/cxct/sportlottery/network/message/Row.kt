package org.cxct.sportlottery.network.message


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Row(
    @Json(name = "id")
    val id: Int,
    @Json(name = "message")
    val message: String,
    @Json(name = "type")
    val type: Long,
    @Json(name = "msgType")
    val messageType: Int,
    @Json(name = "addTime")
    val addTime: String,
    @Json(name = "title")
    val title: String,
    @Json(name = "updateTime")
    val updateTime: String,
    @Json(name = "rechLevels")
    val rechLevels: String,
    @Json(name = "sort")
    val sort: Long,
    @Json(name = "platformId")
    val platformId: Long,
)