package org.cxct.sportlottery.network.matchCategory.result


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class LeagueOdds(
    @Json(name = "league")
    val league: League?,
    @Json(name = "matchOdds")
    val matchOdds: List<OddData>?
)