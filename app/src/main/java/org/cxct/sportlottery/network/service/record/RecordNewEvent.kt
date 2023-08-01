package org.cxct.sportlottery.network.service.record

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.service.ServiceEventType

@KeepMembers
data class RecordNewEvent(
    override val eventType: String,
    val player: String,
    val games: String,
    val betAmount: String,
    var profitAmount: String,
    val iconUrl: String?= null,
    val betTime: Long = 0,
    val firmType: String? = null,
    val gameCode: String? = null
    ) : ServiceEventType