package org.cxct.sportlottery.network.odds.eps


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OddsEpsListData(
    @Json(name = "date")
    val date: Long,
    @Json(name = "leagueOdds")
    val leagueOdd: List<LeagueOdds?>
)