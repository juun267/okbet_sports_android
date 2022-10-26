package org.cxct.sportlottery.network.third_game.third_games.hot

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class HandicapData (
    @Json(name = "league")
    val league:League,
    @Json(name = "matchInfo")
    val matchInfo:MatchInfo,
)