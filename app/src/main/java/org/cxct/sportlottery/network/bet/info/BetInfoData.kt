package org.cxct.sportlottery.network.bet.info


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BetInfoData(
    @Json(name = "matchOdds")
    val matchOdds: MutableList<MatchOdd>,
    @Json(name = "parlayOdds")
    val parlayOdds: MutableList<ParlayOdd>
)