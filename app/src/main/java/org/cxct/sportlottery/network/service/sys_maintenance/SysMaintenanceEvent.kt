package org.cxct.sportlottery.network.service.sys_maintenance

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true) @KeepMembers
data class SysMaintenanceEvent(
    @Json(name = "eventType")
    override val eventType: String? = EventType.SYS_MAINTENANCE,
    @Json(name = "status")
    val status: Int? = null //0 关闭 ，1 维护中
) : ServiceEventType