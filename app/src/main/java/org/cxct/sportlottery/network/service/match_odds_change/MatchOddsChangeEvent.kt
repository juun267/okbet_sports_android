package org.cxct.sportlottery.network.service.match_odds_change


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchOddsChangeEvent(
    @Json(name = "eventId")
    val eventId: String,
    @Json(name = "eventType")
    val eventType: String,
    @Json(name = "isLongTermEvent")
    val isLongTermEvent: Int,
    @Json(name = "odds")
    val odds: Map<String, Odds>
)
