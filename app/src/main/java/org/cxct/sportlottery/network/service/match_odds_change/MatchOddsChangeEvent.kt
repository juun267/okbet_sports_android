package org.cxct.sportlottery.network.service.match_odds_change


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true)
data class MatchOddsChangeEvent(
    @Json(name = "eventType")
    override val eventType: String = EventType.MATCH_ODDS_CHANGE.value,
    @Json(name = "eventId")
    val eventId: String,
    @Json(name = "isLongTermEvent")
    val isLongTermEvent: Int,
    @Json(name = "odds")
    val odds: Map<String, Odds>
): ServiceEventType
