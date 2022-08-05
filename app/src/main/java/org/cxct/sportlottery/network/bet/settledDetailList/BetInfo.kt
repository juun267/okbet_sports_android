package org.cxct.sportlottery.network.bet.settledDetailList

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BetInfo(
    @Json(name = "playMaxBetSingleBet")
    val playMaxBetSingleBet: Long?,
    @Json(name = "maxBetMoney")
    val maxBetMoney: Int?,
    @Json(name = "minBetMoney")
    val minBetMoney: Int?,
    @Json(name = "maxPayout")
    val maxPayout: Int?,
    @Json(name = "maxParlayBetMoney")
    val maxParlayBetMoney: Int?,
    @Json(name = "minParlayBetMoney")
    val minParlayBetMoney: Int?,
    @Json(name = "maxParlayPayout")
    val maxParlayPayout: Int?,
    @Json(name = "maxCpBetMoney")
    val maxCpBetMoney: Int?,
    @Json(name = "minCpBetMoney")
    val minCpBetMoney: Int?,
    @Json(name = "maxCpPayout")
    val maxCpPayout: Int?
)
