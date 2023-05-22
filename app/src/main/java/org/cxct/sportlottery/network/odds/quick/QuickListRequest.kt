package org.cxct.sportlottery.network.odds.quick

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class QuickListRequest(
    val matchId: String
)
