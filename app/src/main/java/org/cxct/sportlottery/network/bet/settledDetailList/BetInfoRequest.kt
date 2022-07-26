package org.cxct.sportlottery.network.bet.settledDetailList

data class BetInfoRequest(
    val matchId: String, //赛事编号
    val oid: String //赔率编号
)