package org.cxct.sportlottery.network.odds.detail

data class OddsDetailRequest(
    val matchId: String,
    val oddsType: String = "EU"
)