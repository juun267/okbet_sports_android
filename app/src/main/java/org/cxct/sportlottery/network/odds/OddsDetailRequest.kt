package org.cxct.sportlottery.network.odds

data class OddsDetailRequest(
    val matchId: String,
    val oddsType: String
)