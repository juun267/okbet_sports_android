package org.cxct.sportlottery.network.league

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class LeagueListRequest(
    val gameType: String,
    val matchType: String,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    var playCateMenuCode: String? = null,
    val isMobile: Int = 1,
)
