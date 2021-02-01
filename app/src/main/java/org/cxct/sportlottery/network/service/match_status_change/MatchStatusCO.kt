package org.cxct.sportlottery.network.service.match_status_change


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchStatusCO(
    @Json(name = "awayCards")
    val awayCards: Int? = -1,
    @Json(name = "awayCornerKicks")
    val awayCornerKicks: Int? = -1,
    @Json(name = "awayScore")
    val awayScore: Int ?= -1,
    @Json(name = "awayTotalScore")
    val awayTotalScore: Int ?= -1,
    @Json(name = "awayYellowCards")
    val awayYellowCards: Int ?= -1,
    @Json(name = "dataId")
    val dataId: String? = "",
    @Json(name = "homeCards")
    val homeCards: Int ?= -1,
    @Json(name = "homeCornerKicks")
    val homeCornerKicks: Int ?= -1,
    @Json(name = "homeScore")
    val homeScore: Int ?= -1,
    @Json(name = "homeTotalScore")
    val homeTotalScore: Int ?= -1,
    @Json(name = "homeYellowCards")
    val homeYellowCards: Int ?= -1,
    @Json(name = "latestStatus")
    val latestStatus: Int ?= -1,
    @Json(name = "matchId")
    val matchId: String? = "",
    @Json(name = "status")
    val status: Int ?= -1,
    @Json(name = "statusName")
    val statusName: String? = "",
    @Json(name = "statusNameI18n")
    val statusNameI18n: StatusNameI18n? = StatusNameI18n(),
    @Json(name = "time")
    val time: String? = ""
)