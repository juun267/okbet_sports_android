package org.cxct.sportlottery.network.bet.settledDetailList


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.settledList.PlayCateMatchResult
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.AppManager
import kotlin.coroutines.coroutineContext

@Parcelize
@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "oddsId")
    val oddsId: String?,
    @Json(name = "matchId")
    val matchId: String?,
    @Json(name = "homeName")
    val homeName: String?,
    @Json(name = "homeId")
    val homeId: String?,
    @Json(name = "awayName")
    val awayName: String?,
    @Json(name = "awayId")
    val awayId: String?,
    @Json(name = "playCateName")
    val playCateName: String?,
    @Json(name = "playName")
    val playName: String?,
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
    val leagueName: String?,
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
    val oddsType: String?
) : Parcelable