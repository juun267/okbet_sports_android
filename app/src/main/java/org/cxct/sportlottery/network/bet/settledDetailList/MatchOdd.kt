package org.cxct.sportlottery.network.bet.settledDetailList


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.bet.list.EndScoreInfo
import org.cxct.sportlottery.network.bet.settledList.PlayCateMatchResult
import org.cxct.sportlottery.util.replaceSpecialChar

@Parcelize
@JsonClass(generateAdapter = true)
@KeepMembers
data class MatchOdd(
    @Json(name = "oddsId")
    val oddsId: String?,
    @Json(name = "matchId")
    val matchId: String?,
    @Json(name = "homeName")
    var homeName: String?,
    @Json(name = "homeId")
    val homeId: String?,
    @Json(name = "awayName")
    var awayName: String?,
    @Json(name = "awayId")
    val awayId: String?,
    @Json(name = "playCateName")
    var playCateName: String?,
    @Json(name = "playName")
    var playName: String?,
    @Json(name = "spread")
    val spread: String?,
    @Json(name = "extInfo")
    val extInfo: String?,
    @Json(name = "odds")
    val odds: Double?,
    @Json(name = "hkOdds")
    val hkOdds: Double?,
    @Json(name = "malayOdds")
    var malayOdds: Double,
    @Json(name = "indoOdds")
    var indoOdds: Double,
    @Json(name = "leagueName")
    var leagueName: String?,
    @Json(name = "leagueId")
    val leagueId: String?,
    @Json(name = "playId")
    val playId: Int?,
    @Json(name = "playCateId")
    val playCateId: Int?,
    @Json(name = "playCateCode")
    val playCateCode: String?,
    @Json(name = "playCateMatchResult")
    val playCateMatchResult: String?,
    @Json(name = "rtScore")
    val rtScore: String?,
    @Json(name = "startTime")
    val startTime: Long?,
    @Json(name = "endTime")
    val endTime: Long?,
    @Json(name = "status")
    val status: Int?,
    @Json(name = "statusNameI18n")
    val statusNameI18n: String?,
    @Json(name = "playCateMatchResultList")
    val playCateMatchResultList: List<PlayCateMatchResult>?,
    @Json(name = "oddsType")
    val oddsType: String?,
    @Json(name = "categoryIcon")
    val categoryIcon: String?,
    @Json(name = "multiCode")
    val multiCode: List<EndScoreInfo>?,
) : Parcelable{
    init {
        leagueName = leagueName?.replaceSpecialChar("\n")
        homeName = homeName?.replaceSpecialChar("\n")
        awayName = awayName?.replaceSpecialChar("\n")
        playCateName = playCateName?.replaceSpecialChar("\n")
        playName = playName?.replaceSpecialChar("\n")
    }
}