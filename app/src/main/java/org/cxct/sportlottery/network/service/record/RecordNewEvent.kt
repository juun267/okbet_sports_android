package org.cxct.sportlottery.network.service.record

import org.cxct.sportlottery.common.extentions.isEmptyStr
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
    val firmType: String = "",
    val gameCode: String? = null // 如果gameCode为空则是体育投注
    ) : ServiceEventType {

        fun isSportBet() = gameCode == null

    }