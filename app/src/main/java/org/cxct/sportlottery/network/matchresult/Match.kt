package org.cxct.sportlottery.network.matchresult


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Match(
    @Json(name = "matchInfo")
    val matchInfo: MatchInfo,
    @Json(name = "matchStatusList")
    val matchStatusList: List<MatchStatus>
)