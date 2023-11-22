package org.cxct.sportlottery.network.service.sys_maintenance

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceEventType


@JsonClass(generateAdapter = true) @KeepMembers
data class SportMaintenanceEvent(
    @Json(name = "eventType")
    override val eventType: String? = EventType.SPORT_MAINTAIN_STATUS,
    @Json(name = "status")
    val status: Int? = null //1开启维护  0关闭
) : ServiceEventType {
    fun isMaintenance() = 1 == status
}
