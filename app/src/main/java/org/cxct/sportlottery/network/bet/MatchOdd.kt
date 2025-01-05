package org.cxct.sportlottery.network.bet


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.bet.list.EndScoreInfo
import org.cxct.sportlottery.network.bet.list.EndingCardOFLWinnable
import org.cxct.sportlottery.util.replaceSpecialChar

@Parcelize
@JsonClass(generateAdapter = true) @KeepMembers
data class MatchOdd(
    @Json(name = "awayId")
    val awayId: String,
    @Json(name = "awayName")
    var awayName: String,
    @Json(name = "homeId")
    val homeId: String,
    @Json(name = "homeName")
    var homeName: String,
    @Json(name = "leagueName")
    var leagueName: String,
    @Json(name = "matchId")
    val matchId: String,
    @Json(name = "odds")
    val odds: Double,
    @Json(name = "hkOdds")
    val hkOdds: Double,
    @Json(name = "malayOdds")
    var malayOdds: Double,
    @Json(name = "indoOdds")
    var indoOdds: Double,
    @Json(name = "oddsId")
    val oddsId: String,
    @Json(name = "playCateCode")
    val playCateCode: String?,
    @Json(name = "playCateId")
    val playCateId: Int,
    @Json(name = "playCateName")
    var playCateName: String,
    @Json(name = "playId")
    val playId: Int,
    @Json(name = "playName")
    var playName: String,
    @Json(name = "spread")
    val spread: String,
    @Json(name = "startTime")
    val startTime: Long?,
    @Json(name = "status")
    val status: Int,
    @Json(name = "oddsType")
    val oddsType: String,
    @Json(name = "rtScore")
    val rtScore: String?,
    @Json(name = "categoryIcon")
    val categoryIcon: String?,
    @Json(name = "leagueIcon")
    val leagueIcon: String?,
    @Json(name = "multiCode")
    val multiCode: List<EndScoreInfo>?,
    @Json(name = "cardMoney")
    val cardMoney: Int?,
    @Json(name = "homeIcon")
    val homeIcon: String?,
    @Json(name = "awayIcon")
    val awayIcon: String?,
    @Json(name = "maximumWinnable")
    val maximumWinnable: Double?,
    @Json(name = "endingCardOFLWinnable")
    val endingCardOFLWinnable: EndingCardOFLWinnable?,
): Parcelable{
    init {
        leagueName = leagueName.replaceSpecialChar("\n")
        homeName = homeName.replaceSpecialChar("\n")
        awayName = awayName.replaceSpecialChar("\n")
        playCateName = playCateName.replaceSpecialChar("\n")
        playName = playName.replaceSpecialChar("\n")
    }
}