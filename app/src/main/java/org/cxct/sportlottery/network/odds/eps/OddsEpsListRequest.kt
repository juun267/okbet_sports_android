package org.cxct.sportlottery.network.odds.eps

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class OddsEpsListRequest(
    val gameType: String,
    val matchType: String,
    val startTime: Long
)