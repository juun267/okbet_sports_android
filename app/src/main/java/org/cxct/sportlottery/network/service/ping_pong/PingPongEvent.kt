package org.cxct.sportlottery.network.service.ping_pong

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true) @KeepMembers
data class PingPongEvent(
    @Json(name = "eventType")
    override val eventType: String? = EventType.PING_PONG.value,
    @Json(name = "message")
    val message: String? //返回消息，正常消息： pong, 已过期： timeout
) : ServiceEventType