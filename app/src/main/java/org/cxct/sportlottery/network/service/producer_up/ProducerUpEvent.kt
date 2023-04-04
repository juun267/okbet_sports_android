package org.cxct.sportlottery.network.service.producer_up


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true) @KeepMembers
data class ProducerUpEvent(
    @Json(name = "eventType")
    override val eventType: String? = EventType.PRODUCER_UP,
    @Json(name = "producerId")
    val producerId: Int?,
) : ServiceEventType