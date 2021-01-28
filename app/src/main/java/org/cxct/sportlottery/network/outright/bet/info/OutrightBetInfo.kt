package org.cxct.sportlottery.network.outright.bet.info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OutrightBetInfo(
    @Json(name = "matchOdds")
    val matchOdds: List<MatchOdd>,
    @Json(name = "parlayOdds")
    val parlayOdds: List<ParlayOdd>
)