package org.cxct.sportlottery.network.matchresult.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchInfo(
    @Json(name = "awayName")
    val awayName: String,
    @Json(name = "endTime")
    val endTime: String?,
    @Json(name = "homeName")
    val homeName: String,
    @Json(name = "id")
    val id: String,
    @Json(name = "playCateNum")
    val playCateNum: Int?,
    @Json(name = "startTime")
    val startTime: String,
    @Json(name = "status")
    val status: Int
)