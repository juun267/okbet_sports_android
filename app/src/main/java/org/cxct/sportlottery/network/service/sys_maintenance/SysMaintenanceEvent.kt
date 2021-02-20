package org.cxct.sportlottery.network.service.sys_maintenance

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true)
data class SysMaintenanceEvent(
    @Json(name = "eventType")
    override val eventType: String = EventType.SYS_MAINTENANCE.value,
    @Json(name = "status")
    val status: Int? = null
): ServiceEventType