package org.cxct.sportlottery.network.odds.eps

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.odds.League
import org.cxct.sportlottery.network.odds.list.MatchOddsItem

@JsonClass(generateAdapter = true)
data class LeagueOdds(
    @Json(name = "league")
    val league: League,
    @Json(name = "matchOdds")
    val matchOdds: MutableList<MatchOddsItem>
)
