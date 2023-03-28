package org.cxct.sportlottery.network.bet.settledList


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Other(
    @Json(name = "addTime")
    val addTime: String?,
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
    val rebateAmount: Int?,
    @Json(name = "settleTime")
    val settleTime: String?,
    @Json(name = "stake")
    val stake: Int?,
    @Json(name = "status")
    val status: Int?,
    @Json(name = "totalAmount")
    val totalAmount: Int?,
    @Json(name = "userId")
    val userId: Int?,
    @Json(name = "userName")
    val userName: String?,
    @Json(name = "win")
    val win: Int?,
    @Json(name = "winnable")
    val winnable: Double?
)