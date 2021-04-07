package org.cxct.sportlottery.network.outright.odds

data class OutrightOddsListRequest(
    val gameType: String,
    val matchType: String = "TODAY",
    val playCateMenuCode: String = "OUTRIGHT",
    val leagueIdList: List<String>? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null
)
