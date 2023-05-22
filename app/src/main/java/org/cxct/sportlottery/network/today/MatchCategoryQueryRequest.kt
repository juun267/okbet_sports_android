package org.cxct.sportlottery.network.today

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class MatchCategoryQueryRequest(
    val gameType: String,
    val matchType: String,
    val now: String,
    val todayStart: String
)
