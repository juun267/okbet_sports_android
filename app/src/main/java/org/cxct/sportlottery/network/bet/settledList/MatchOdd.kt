package org.cxct.sportlottery.network.bet.settledList


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.util.replaceSpecialChar

@JsonClass(generateAdapter = true) @KeepMembers
data class MatchOdd(
    @Json(name = "awayId")
    val awayId: String?,
    @Json(name = "awayName")
    var awayName: String?,
    @Json(name = "endTime")
    val endTime: String?,
    @Json(name = "extInfo")
    val extInfo: String?,
    @Json(name = "hkOdds")
    val hkOdds: Int?,
    @Json(name = "homeId")
    val homeId: String?,
    @Json(name = "homeName")
    var homeName: String?,
    @Json(name = "leagueName")
    var leagueName: String?,
    @Json(name = "matchId")
    val matchId: String?,
    @Json(name = "odds")
    val odds: Float?,
    @Json(name = "oddsId")
    val oddsId: String?,
    @Json(name = "playCateCode")
    val playCateCode: String?,
    @Json(name = "playCateId")
    val playCateId: Int?,
    @Json(name = "playCateMatchResult")
    val playCateMatchResult: String?,
    @Json(name = "playCateMatchResultList")
    val playCateMatchResultList: List<PlayCateMatchResult>?,
    @Json(name = "playCateName")
    var playCateName: String?,
    @Json(name = "playId")
    val playId: Int?,
    @Json(name = "playName")
    var playName: String?,
    @Json(name = "spread")
    val spread: String?,
    @Json(name = "startTime")
    val startTime: String?,
    @Json(name = "status")
    val status: Int?,
    @Json(name = "statusNameI18n")
    val statusNameI18n: Map<String, String>?
){
    init {
        leagueName = leagueName?.replaceSpecialChar("\n")
        homeName = homeName?.replaceSpecialChar("\n")
        awayName = awayName?.replaceSpecialChar("\n")
        playCateName = playCateName?.replaceSpecialChar("\n")
        playName = playName?.replaceSpecialChar("\n")
    }
}