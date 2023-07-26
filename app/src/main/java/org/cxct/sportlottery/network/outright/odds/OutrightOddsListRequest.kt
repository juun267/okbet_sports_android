package org.cxct.sportlottery.network.outright.odds

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class OutrightOddsListRequest(
    val gameType: String,
    val matchType: String,
    val playCateMenuCode: String = "OUTRIGHT",
    val leagueIdList: List<String>? = null,
    val matchIdList: List<String>? = null,
)
