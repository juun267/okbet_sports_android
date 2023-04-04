package org.cxct.sportlottery.network.service.match_clock


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true) @KeepMembers
data class MatchClockEvent(
    @Json(name = "eventType")
    override val eventType: String? = EventType.MATCH_CLOCK,
    @Json(name = "matchClockCO")
    val matchClockCO: MatchClockCO?
) : ServiceEventType
