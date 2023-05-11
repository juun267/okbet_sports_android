package org.cxct.sportlottery.network.bet.settledDetailList


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@JsonClass(generateAdapter = true) @KeepMembers
data class Row (
    @Json(name = "addTime")
    val addTime: Long?,
    @Json(name = "cancelReason")
    val cancelReason: String?,
    @Json(name = "cancelledBy")
    val cancelledBy: String?,
    @Json(name = "gameType")
    val gameType: String?,
    @Json(name = "grossWin")
    val grossWin: Double?,
    @Json(name = "matchOdds")
    val matchOdds: List<MatchOdd>?,
    @Json(name = "matchType")
    val matchType: String?,
    @Json(name = "netWin")
    val netWin: Double?,
    @Json(name = "num")
    val num: Int?,
    @Json(name = "orderNo")
    val orderNo: String?,
    @Json(name = "parlayType")
    val parlayType: String?,
    @Json(name = "rebate")
    val rebate: Double?,
    @Json(name = "rebateAmount")
    val rebateAmount: Double?,
    @Json(name = "settleTime")
    val settleTime: String?,
    @Json(name = "stake")
    val stake: Double?,
    @Json(name = "status")
    val status: Int?,
    @Json(name = "totalAmount")
    val totalAmount: Double?,
    @Json(name = "userId")
    val userId: Int?,
    @Json(name = "userName")
    val userName: String?,
    @Json(name = "win")
    val win: Double?,
    @Json(name = "winnable")
    val winnable: Double?,
    @Json(name = "leagueId")
    val leagueId: String?,
    @Json(name = "matchId")
    val matchId: String?,
    @Json(name = "rtScore")
    val rtScore: String?,
    @Json(name = "parlayComsDetailVOs")
    val parlayComsDetailVOs: List<ParlayComsDetailVO>?,
) : Parcelable {
}