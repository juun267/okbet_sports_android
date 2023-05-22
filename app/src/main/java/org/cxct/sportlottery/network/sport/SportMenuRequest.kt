package org.cxct.sportlottery.network.sport

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class SportMenuRequest(
    val now: String,
    val todayStart: String
)
