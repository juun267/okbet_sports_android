package org.cxct.sportlottery.network.odds


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
    val id: String, //赛事或赛季id
    @Json(name = "playCateNum")
    val playCateNum: Int,
    @Json(name = "startTime")
    val startTime: String,
    @Json(name = "status")
    val status: Int
)