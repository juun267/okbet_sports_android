package org.cxct.sportlottery.network.service.user_notice

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true) @KeepMembers
data class UserNoticeEvent(
    @Json(name = "eventType")
    override val eventType: String? = EventType.USER_NOTICE.value,
    @Json(name = "userNoticeList")
    val userNoticeList: List<UserNotice>? = listOf()
) : ServiceEventType