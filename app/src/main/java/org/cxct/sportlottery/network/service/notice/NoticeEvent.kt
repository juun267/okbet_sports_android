package org.cxct.sportlottery.network.service.notice


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true) @KeepMembers
data class NoticeEvent(
    @Json(name = "eventType")
    override val eventType: String? = EventType.NOTICE.value,
    @Json(name = "message")
    val message: String?, //公告消息
) : ServiceEventType