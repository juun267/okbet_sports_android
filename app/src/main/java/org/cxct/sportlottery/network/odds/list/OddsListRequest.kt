package org.cxct.sportlottery.network.odds.list

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class OddsListRequest(
    val gameType: String,
    val matchType: String,
    val playCateMenuCode: String,
    val leagueIdList: List<String>? = null,
    val matchIdList: List<String>? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null
)
