package org.cxct.sportlottery.network.outright.add


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "awayId")
    val awayId: String,
    @Json(name = "awayName")
    val awayName: String,
    @Json(name = "homeId")
    val homeId: String,
    @Json(name = "homeName")
    val homeName: String,
    @Json(name = "leagueName")
    val leagueName: String,
    @Json(name = "matchId")
    val matchId: String,
    @Json(name = "odds")
    val odds: Int,
    @Json(name = "oddsId")
    val oddsId: String,
    @Json(name = "oddsType")
    val oddsType: String,
    @Json(name = "playCateId")
    val playCateId: Int,
    @Json(name = "playCateMatchResult")
    val playCateMatchResult: String,
    @Json(name = "playCateName")
    val playCateName: String,
    @Json(name = "playId")
    val playId: Int,
    @Json(name = "playName")
    val playName: String,
    @Json(name = "spread")
    val spread: String,
    @Json(name = "startTime")
    val startTime: String,
    @Json(name = "status")
    val status: Int,
    @Json(name = "statusNameI18n")
    val statusNameI18n: StatusNameI18n
)