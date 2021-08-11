package org.cxct.sportlottery.network.odds.list

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.odds.MatchInfo

@JsonClass(generateAdapter = true)
data class MatchOddsItem(
    @Json(name = "matchInfo")
    val matchInfo: MatchInfo? = null,
    @Json(name = "odds")
    val odds: Odds?
)
