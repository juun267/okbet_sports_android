package org.cxct.sportlottery.network.odds.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true)
@KeepMembers
data class OddsListData(
    @Json(name = "leagueOdds")
    val leagueOdds: List<LeagueOdd>?
) {
    var leagueOddsFilter: List<LeagueOdd>? = null
}