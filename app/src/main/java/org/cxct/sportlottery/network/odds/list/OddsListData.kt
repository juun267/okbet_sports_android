package org.cxct.sportlottery.network.odds.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OddsListData(
    @Json(name = "leagueOdds")
    val leagueOdds: List<LeagueOdd>,
    @Json(name = "sport")
    val sport: Sport
)