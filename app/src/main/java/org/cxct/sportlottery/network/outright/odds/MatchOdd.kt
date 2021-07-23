package org.cxct.sportlottery.network.outright.odds

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.odds.Odd

@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "matchInfo")
    val matchInfo: MatchInfo,
    @Json(name = "odds")
    val odds: Map<String, List<Odd?>>,
    @Json(name = "dynamicMarkets")
    val dynamicMarkets: Map<String, DynamicMarket>
) {
    var startDate: String = ""
    var startTime: String = ""
}