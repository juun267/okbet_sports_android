package org.cxct.sportlottery.network.outright.bet.info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "awayName")
    val awayName: String,
    @Json(name = "gameType")
    val gameType: String,
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
    val odds: Int,
    @Json(name = "oddsId")
    val oddsId: String,
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
    val startTime: String,
    @Json(name = "status")
    val status: Int
)