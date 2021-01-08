package org.cxct.sportlottery.network.odds.list

data class OddsListRequest(
    val gameType: String,
    val matchType: String,
    val oddsType: String = "EU",
    val playCateMenuCode: String = "HDP&OU",
    val leagueIdList: List<String>? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null
)
