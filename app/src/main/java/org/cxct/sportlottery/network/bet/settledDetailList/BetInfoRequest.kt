package org.cxct.sportlottery.network.bet.settledDetailList

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class BetInfoRequest(
    val matchId: String, //赛事编号
    val oid: String //赔率编号
)