package org.cxct.sportlottery.network.bet.add.betReceipt


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Receipt(
    @Json(name = "parlayBets")
    val parlayBets: List<BetResult>?,
    @Json(name = "singleBets")
    val singleBets: List<BetResult>?,
    @Json(name = "totalStake")
    val totalStake: Double?,
    @Json(name = "totalWinnable")
    val totalWinnable: Double?,
    @Json(name = "totalNum")
    val totalNum: Int?,
    @Json(name = "userPlayAmount")
    val userPlayAmount: Double?,
    @Json(name = "betConfirmTime")
    val betConfirmTime: Long? = null
)