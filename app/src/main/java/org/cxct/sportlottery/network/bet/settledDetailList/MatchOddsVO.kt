package org.cxct.sportlottery.network.bet.settledDetailList

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.util.replaceSpecialChar

@Parcelize
@JsonClass(generateAdapter = true)
@KeepMembers
data class MatchOddsVO(
    @Json(name = "awayId")
    val awayId: String?,
    @Json(name = "awayName")
    var awayName: String?,
    @Json(name = "endTime")
    val endTime: String?,
    @Json(name = "extInfo")
    val extInfo: String?,
    @Json(name = "hkOdds")
    val hkOdds: Double?,
    @Json(name = "homeId")
    val homeId: String?,
    @Json(name = "homeName")
    var homeName: String?,
    @Json(name = "indoOdds")
    val indoOdds: Double?,
    @Json(name = "leagueName")
    var leagueName: String?,
    @Json(name = "malayOdds")
    val malayOdds: Double?,
    @Json(name = "matchId")
    val matchId: String?,
    @Json(name = "odds")
    val odds: Double,
    @Json(name = "oddsId")
    val oddsId: String?,
    @Json(name = "oddsType")
    val oddsType: String?,
    @Json(name = "playCateCode")
    val playCateCode: String?,
    @Json(name = "playCateId")
    val playCateId: Int?,
    @Json(name = "playCateMatchResult")
    val playCateMatchResult: String?,
    @Json(name = "playCateName")
    var playCateName: String?,
    @Json(name = "playCode")
    val playCode: String?,
    @Json(name = "playId")
    val playId: Int?,
    @Json(name = "playName")
    var playName: String?,
    @Json(name = "rtScore")
    val rtScore: String?,
    @Json(name = "spread")
    val spread: String?,
    @Json(name = "startTime")
    val startTime: Long?,
    @Json(name = "startTimeDesc")
    val startTimeDesc: String?,
    @Json(name = "status")
    val status: Int?,
    var categoryIcon:String
): Parcelable{
    init {
        leagueName = leagueName?.replaceSpecialChar("\n")
        homeName = homeName?.replaceSpecialChar("\n")
        awayName = awayName?.replaceSpecialChar("\n")
        playCateName = playCateName?.replaceSpecialChar("\n")
        playName = playName?.replaceSpecialChar("\n")
    }
}

