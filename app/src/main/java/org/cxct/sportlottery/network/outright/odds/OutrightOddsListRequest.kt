package org.cxct.sportlottery.network.outright.odds

data class OutrightOddsListRequest(
    val gameType: String,
    val playCateMenuCode: String = "OUTRIGHT",
    val leagueIdList: List<String>? = null,
)
