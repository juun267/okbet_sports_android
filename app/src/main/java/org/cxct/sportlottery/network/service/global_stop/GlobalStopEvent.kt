package org.cxct.sportlottery.network.service.global_stop


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true) @KeepMembers
data class GlobalStopEvent(
    @Json(name = "eventType")
    override val eventType: String? = EventType.GLOBAL_STOP.value,
    @Json(name = "producerId")
    val producerId: Int?,
) : ServiceEventType