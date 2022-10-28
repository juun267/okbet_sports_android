package org.cxct.sportlottery.network.third_game.third_games.hot

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.odds.League
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend

@JsonClass(generateAdapter = true)
data class HandicapData(
    @Json(name = "league")
    val league: League,
    @Json(name = "matchInfos")
    val matchInfos: List<HotMatchInfo>,
    @Json(name = "sportName")
    val sportName: String,
)