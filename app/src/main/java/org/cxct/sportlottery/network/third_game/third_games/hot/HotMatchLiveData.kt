package org.cxct.sportlottery.network.third_game.third_games.hot


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

import org.cxct.sportlottery.network.odds.League
import org.cxct.sportlottery.network.odds.MatchInfo


@JsonClass(generateAdapter = true)
data class HotMatchLiveData(
    @Json(name = "league")
    val league: League,
    @Json(name = "matchInfo")
    val matchInfo: MatchInfo,
    @Json(name = "sportName")
    val sportName: String
)