package org.cxct.sportlottery.network.odds.eps

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.odds.League

@JsonClass(generateAdapter = true)
data class EpsLeagueOddsItem(
    @Json(name = "date")
    var date: Long = 0,
    @Json(name = "leagueOdds")
    val leagueOdds: LeagueOdds?
){
    var isClose = true
}
