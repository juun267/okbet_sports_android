package org.cxct.sportlottery.network.service.match_status_change


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchStatusCO(
    @Json(name = "gameType")
    val gameType: String?,
    @Json(name = "awayCards")
    val awayCards: Int? = null,
    @Json(name = "awayCornerKicks")
    val awayCornerKicks: Int? = null,
    @Json(name = "awayScore")
    val awayScore: Int ?= null,
    @Json(name = "awayTotalScore")
    val awayTotalScore: Int ?= null,
    @Json(name = "awayYellowCards")
    val awayYellowCards: Int ?= null,
    @Json(name = "dataId")
    val dataId: String? = "",
    @Json(name = "homeCards")
    val homeCards: Int ?= null,
    @Json(name = "homeCornerKicks")
    val homeCornerKicks: Int ?= null,
    @Json(name = "homeScore")
    val homeScore: Int ?= null,
    @Json(name = "homeTotalScore")
    val homeTotalScore: Int ?= null,
    @Json(name = "homeYellowCards")
    val homeYellowCards: Int ?= null,
    @Json(name = "latestStatus")
    val latestStatus: Int ?= null,
    @Json(name = "matchId")
    val matchId: String? = "",
    @Json(name = "status")
    val status: Int ?= null,
    @Json(name = "statusName")
    val statusName: String? = "",
    @Json(name = "statusNameI18n")
    val statusNameI18n: StatusNameI18n? = StatusNameI18n(),
    @Json(name = "time")
    val time: String? = ""
)