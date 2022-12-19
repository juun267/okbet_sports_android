package org.cxct.sportlottery.network.outright.odds


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OutrightOddsListData(
    @Json(name = "leagueOdds")
    val leagueOdds: List<LeagueOdd>? = listOf(),
    @Json(name = "sport")
    val sport: Sport?
)