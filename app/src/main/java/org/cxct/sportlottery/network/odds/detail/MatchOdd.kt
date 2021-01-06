package org.cxct.sportlottery.network.odds.detail


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.odds.MatchInfo

@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "matchInfo")
    val matchInfo: MatchInfo,
    @Json(name = "odds")
    val odds: Map<String, CateDetailData>
)