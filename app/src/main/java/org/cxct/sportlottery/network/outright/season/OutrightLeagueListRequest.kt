package org.cxct.sportlottery.network.outright.season

data class OutrightLeagueListRequest(
    val gameType: String,
    val matchType:String? = null
)
