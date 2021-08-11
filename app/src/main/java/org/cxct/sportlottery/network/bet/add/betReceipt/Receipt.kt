package org.cxct.sportlottery.network.bet.add.betReceipt


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Receipt(
    @Json(name = "parlayBets")
    val parlayBets: List<BetResult>?,
    @Json(name = "singleBets")
    val singleBets: List<BetResult>?
)