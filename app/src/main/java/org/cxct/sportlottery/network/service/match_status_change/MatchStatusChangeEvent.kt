package org.cxct.sportlottery.network.service.match_status_change


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true)
data class MatchStatusChangeEvent(
    @Json(name = "eventType")
    override val eventType: String = EventType.MATCH_STATUS_CHANGE.value,
    @Json(name = "matchStatusCO")
    val matchStatusCO: MatchStatusCO?
): ServiceEventType