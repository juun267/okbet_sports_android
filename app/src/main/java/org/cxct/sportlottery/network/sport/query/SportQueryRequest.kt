package org.cxct.sportlottery.network.sport.query

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class SportQueryRequest(
    val now: String,
    val todayStart: String,
    val matchType: String,
    val matchIdList: List<String>? = null,
    val leagueIdList: List<String>? = null
)