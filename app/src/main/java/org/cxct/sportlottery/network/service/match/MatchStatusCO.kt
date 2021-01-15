package org.cxct.sportlottery.network.service.match


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchStatusCO(
    @Json(name = "awayCards")
    val awayCards: Int,
    @Json(name = "awayCornerKicks")
    val awayCornerKicks: Int,
    @Json(name = "awayScore")
    val awayScore: Int,
    @Json(name = "awayTotalScore")
    val awayTotalScore: Int,
    @Json(name = "awayYellowCards")
    val awayYellowCards: Int,
    @Json(name = "dataId")
    val dataId: Any,
    @Json(name = "homeCards")
    val homeCards: Int,
    @Json(name = "homeCornerKicks")
    val homeCornerKicks: Int,
    @Json(name = "homeScore")
    val homeScore: Int,
    @Json(name = "homeTotalScore")
    val homeTotalScore: Int,
    @Json(name = "homeYellowCards")
    val homeYellowCards: Int,
    @Json(name = "latestStatus")
    val latestStatus: Int,
    @Json(name = "matchId")
    val matchId: String,
    @Json(name = "status")
    val status: Int,
    @Json(name = "statusName")
    val statusName: String,
    @Json(name = "statusNameI18n")
    val statusNameI18n: StatusNameI18n,
    @Json(name = "time")
    val time: String?
)