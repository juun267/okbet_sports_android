package org.cxct.sportlottery.network.matchresult.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class MatchStatus(
    @Json(name = "awayCards")
    val awayCards: Int?,
    @Json(name = "awayCornerKicks")
    val awayCornerKicks: Int?,
    @Json(name = "awayScore")
    val awayScore: Int?,
    @Json(name = "awayYellowCards")
    val awayYellowCards: Int?,
    @Json(name = "homeCards")
    val homeCards: Int?,
    @Json(name = "homeCornerKicks")
    val homeCornerKicks: Int?,
    @Json(name = "homeScore")
    val homeScore: Int?,
    @Json(name = "homeYellowCards")
    val homeYellowCards: Int?,
    @Json(name = "matchId")
    val matchId: String,
    @Json(name = "status")
    val status: Int,
    @Json(name = "statusName")
    val statusName: String,
    @Json(name = "awayTotalScore")
    val awayTotalScore: Int?,
    @Json(name = "homeTotalScore")
    val homeTotalScore: Int?,
)