package org.cxct.sportlottery.network.odds.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class OddsListData(
    @Json(name = "leagueOdds")
    val leagueOdds: List<LeagueOdd>,
    @Json(name = "sport")
    val sport: Sport
) {
    var leagueOddsFilter: List<LeagueOdd>? = null
}