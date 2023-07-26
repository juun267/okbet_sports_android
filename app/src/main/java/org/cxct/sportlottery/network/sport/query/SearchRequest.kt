package org.cxct.sportlottery.network.sport.query

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class SearchRequest(
    val now: String,
    val todayStart: String
)