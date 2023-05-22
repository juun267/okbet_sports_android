package org.cxct.sportlottery.network.outright.season

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class OutrightLeagueListRequest(
    val gameType: String,
    val matchType:String? = null
)
