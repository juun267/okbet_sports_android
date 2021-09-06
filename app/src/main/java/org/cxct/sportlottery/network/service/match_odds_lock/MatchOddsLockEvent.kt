package org.cxct.sportlottery.network.service.match_odds_lock

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true)
data class MatchOddsLockEvent(
    @Json(name = "eventType")
    override val eventType: String?,
    @Json(name = "matchId")
    val matchId: String

): ServiceEventType
