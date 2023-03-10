package org.cxct.sportlottery.network.bet.add.betReceipt


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers
import org.cxct.sportlottery.network.bet.add.StatusNameI18n

@JsonClass(generateAdapter = true) @KeepMembers
data class MatchOdd(
    @Json(name = "awayId")
    val awayId: String,
    @Json(name = "awayName")
    val awayName: String?,
    @Json(name = "endTime")
    val endTime: Long?,
    @Json(name = "extInfo")
    val extInfo: String?,
    @Json(name = "hkOdds")
    val hkOdds: Double?,
    @Json(name = "malayOdds")
    var malayOdds: Double?,
    @Json(name = "indoOdds")
    var indoOdds: Double?,
    @Json(name = "homeId")
    val homeId: String,
    @Json(name = "homeName")
    val homeName: String?,
    @Json(name = "leagueName")
    val leagueName: String?,
    @Json(name = "matchId")
    val matchId: String,
    @Json(name = "odds")
    val odds: Double?,
    @Json(name = "oddsId")
    val oddsId: String?,
    @Json(name = "playCateCode")
    val playCateCode: String?,
    @Json(name = "playCateId")
    val playCateId: Int?,
    @Json(name = "playCateMatchResult")
    val playCateMatchResult: String?,
    @Json(name = "playCateMatchResultList")
    val playCateMatchResultList: String?,
    @Json(name = "playCateName")
    val playCateName: String?,
    @Json(name = "playCode")
    val playCode: String?,
    @Json(name = "playId")
    val playId: Int?,
    @Json(name = "playName")
    val playName: String?,
    @Json(name = "rtScore")
    val rtScore: String?,
    @Json(name = "spread")
    val spread: String?,
    @Json(name = "startTime")
    val startTime: Long?,
    @Json(name = "status")
    val status: Int?,
    @Json(name = "statusNameI18n")
    val statusNameI18n: StatusNameI18n?
)