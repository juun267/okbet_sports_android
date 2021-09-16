package org.cxct.sportlottery.network.odds.list

data class OddsListRequest(
    val gameType: String,
    val matchType: String,
    val playCateMenuCode: String,
    val playCateCodeList: List<String>?,//TODO 9/15確認 playCateCodeList參數，馬克說移除了
    val leagueIdList: List<String>? = null,
    val matchIdList: List<String>? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null
)
