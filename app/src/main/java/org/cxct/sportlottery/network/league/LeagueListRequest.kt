package org.cxct.sportlottery.network.league

data class LeagueListRequest(
    val gameType: String,
    val matchType: String,
    val date: String? = null
)
