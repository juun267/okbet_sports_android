package org.cxct.sportlottery.network.odds

data class OddsListRequest(
    val gameType: String,
    val matchType: String,
    val oddsType: String,
    val playCateMenuCode: String,
    val leagueIdList: List<Int>? = null,
    val date: String? = null
)
