package org.cxct.sportlottery.network.bet.settledDetailList

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BetInfo(
    @Json(name = "playMaxBetSingleBet")
    val playMaxBetSingleBet: Long?,
)
