package org.cxct.sportlottery.network.third_game.third_games.hot

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.odds.League

@JsonClass(generateAdapter = true)
@KeepMembers
data class HandicapData(
    @Json(name = "league")
    val league: League,
    @Json(name = "matchInfos")
    val matchInfos: MutableList<HotMatchInfo>,
    @Json(name = "sportName")
    val sportName: String,
    @Json(name = "oddsSort")
    val oddsSort: String,
)