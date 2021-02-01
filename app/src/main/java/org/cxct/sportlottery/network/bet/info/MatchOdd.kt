package org.cxct.sportlottery.network.bet.info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "awayName")
    val awayName: String,
    @Json(name = "homeName")
    val homeName: String,
    @Json(name = "inplay")
    val inplay: Int,
    @Json(name = "leagueId")
    val leagueId: String,
    @Json(name = "leagueName")
    val leagueName: String,
    @Json(name = "matchId")
    val matchId: String,
    @Json(name = "odds")
    var odds: Double,
    @Json(name = "oddsId")
    var oddsId: String,
    @Json(name = "oddsType")
    val oddsType: String,
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
    @Json(name = "producerId")
    val producerId: Int,
    @Json(name = "spread")
    val spread: String,
    @Json(name = "startTime")
    val startTime: Long,
    @Json(name = "status")
    val status: Int
)