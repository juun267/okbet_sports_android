package org.cxct.sportlottery.network.outright.odds


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class LeagueOdd(
    @Json(name = "league")
    val league: League?,
    @Json(name = "matchOdds")
    val matchOdds: List<MatchOdd> ?= listOf()
) {
    var isExpand = false
}