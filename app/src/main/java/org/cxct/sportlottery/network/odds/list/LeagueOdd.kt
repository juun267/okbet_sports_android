package org.cxct.sportlottery.network.odds.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.odds.League

@JsonClass(generateAdapter = true)
data class LeagueOdd(
    @Json(name = "league")
    val league: League,
    @Json(name = "matchOdds")
    val matchOdds: MutableList<MatchOdd> = mutableListOf(),
    @Json(name = "sort")
    val sort: Int?,
    @Json(name = "unfold")
    var unfold: Int?
) {
    var searchMatchOdds = listOf<MatchOdd>()
    var gameType: GameType? = null
}