package org.cxct.sportlottery.network.odds.detail

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class OddsDetailRequest(
    val matchId: String,
)