package org.cxct.sportlottery.network.matchCategory.result


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchInfo(
    @Json(name = "awayName")
    val awayName: String?,
    @Json(name = "endTime")
    val endTime: Long?,
    @Json(name = "homeName")
    val homeName: String?,
    @Json(name = "id")
    val id: String?,
    @Json(name = "img")
    val img: String?,
    @Json(name = "leagueId")
    val leagueId: String?,
    @Json(name = "leagueName")
    val leagueName: String?,
    @Json(name = "msg")
    val msg: String?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "playCateNum")
    val playCateNum: Int?,
    @Json(name = "startTime")
    val startTime: Long?,
    @Json(name = "status")
    val status: Int?
)