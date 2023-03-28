package org.cxct.sportlottery.network.bet


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true) @KeepMembers
data class MatchOdd(
    @Json(name = "awayId")
    val awayId: String,
    @Json(name = "awayName")
    val awayName: String,
    @Json(name = "homeId")
    val homeId: String,
    @Json(name = "homeName")
    val homeName: String,
    @Json(name = "leagueName")
    val leagueName: String,
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
    val playCateName: String,
    @Json(name = "playId")
    val playId: Int,
    @Json(name = "playName")
    val playName: String,
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
): Parcelable