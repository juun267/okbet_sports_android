package org.cxct.sportlottery.network.odds.list

data class OddsListRequest(
    val gameType: String,
    val matchType: String,
    val playCateMenuCode: String,
    val leagueIdList: List<String> = listOf(),
    val matchIdList: List<String> = listOf(),
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null
)
