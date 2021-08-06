package org.cxct.sportlottery.network.odds.eps

data class OddsEpsListRequest(
    val gameType: String,
    val startTime: String
)