package org.cxct.sportlottery.network.odds


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OddsDetailData(
    @Json(name = "league")
    val league: League,
    @Json(name = "matchOdds")
    val matchOdd: MatchOdd
)