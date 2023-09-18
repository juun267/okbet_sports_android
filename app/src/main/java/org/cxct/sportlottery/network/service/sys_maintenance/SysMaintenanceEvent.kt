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


@JsonClass(generateAdapter = true) @KeepMembers
data class SportMaintenanceEvent(
    @Json(name = "eventType")
    override val eventType: String? = EventType.SPORT_MAINTAIN_STATUS,
    @Json(name = "status")
    val status: Int? = null //1开启维护  0关闭
) : ServiceEventType {
    fun isMaintenance() = 1 == status
}

@JsonClass(generateAdapter = true) @KeepMembers
data class JackpotEvent(
    @Json(name = "eventType")
    override val eventType: String? = EventType.RECORD_RESULT_JACKPOT_OK_GAMES,
    @Json(name = "amount")
    var amount: String = "" //金额
) : ServiceEventType

