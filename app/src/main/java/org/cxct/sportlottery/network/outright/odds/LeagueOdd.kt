package org.cxct.sportlottery.network.outright.odds


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LeagueOdd(
    @Json(name = "league")
    val league: League?,
    @Json(name = "matchOdds")
    val matchOdds: List<MatchOdd> ?= listOf()
) {
    var isExpand = false
}