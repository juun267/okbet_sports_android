package org.cxct.sportlottery.network.matchresult.playlist


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchResultPlayList(
    @Json(name = "matchId")
    val matchId: String,
    @Json(name = "playCateCode")
    val playCateCode: String,
    @Json(name = "playCateId")
    val playCateId: Int,
    @Json(name = "playCateName")
    val playCateName: String,
    @Json(name = "playCode")
    val playCode: String,
    @Json(name = "playId")
    val playId: Int,
    @Json(name = "playName")
    val playName: String,
    @Json(name = "resultStatus")
    val resultStatus: Int,
    @Json(name = "spread")
    val spread: String?,
    @Json(name = "updateTime")
    val updateTime: Long
)