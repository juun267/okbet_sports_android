package org.cxct.sportlottery.network.odds.eps

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers
import org.cxct.sportlottery.network.odds.League

@JsonClass(generateAdapter = true) @KeepMembers
data class LeagueOdds(
    @Json(name = "league")
    val league: League,
    @Json(name = "matchOdds")
    val matchOdds: MutableList<MatchOddsItem>,
    @Json(name = "sort")
    val sort: Int? = null,
    @Json(name = "unfold")
    var unfold: Int? = null
)
