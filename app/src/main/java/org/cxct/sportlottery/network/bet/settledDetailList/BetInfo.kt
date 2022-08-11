package org.cxct.sportlottery.network.bet.settledDetailList

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BetInfo(
    @Json(name = "playMaxBetSingleBet")
    val playMaxBetSingleBet: Long?,
    @Json(name = "maxBetMoney")
    val maxBetMoney: Long?,
    @Json(name = "minBetMoney")
    val minBetMoney: Long?,
    @Json(name = "maxPayout")
    val maxPayout: Long?,
    @Json(name = "maxParlayBetMoney")
    val maxParlayBetMoney: Long?,
    @Json(name = "minParlayBetMoney")
    val minParlayBetMoney: Long?,
    @Json(name = "maxParlayPayout")
    val maxParlayPayout: Long?,
    @Json(name = "maxCpBetMoney")
    val maxCpBetMoney: Long?,
    @Json(name = "minCpBetMoney")
    val minCpBetMoney: Long?,
    @Json(name = "maxCpPayout")
    val maxCpPayout: Long?
)
