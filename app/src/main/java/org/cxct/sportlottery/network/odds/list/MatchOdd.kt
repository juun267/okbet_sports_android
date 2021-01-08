package org.cxct.sportlottery.network.odds.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.odds.MatchInfo

@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "matchInfo")
    val matchInfo: MatchInfo,
    @Json(name = "odds")
    val odds: Map<String, List<Odd>>
) {
    var isExpand = false
}