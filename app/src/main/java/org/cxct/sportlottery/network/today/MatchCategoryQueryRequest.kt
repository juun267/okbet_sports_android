package org.cxct.sportlottery.network.today

data class MatchCategoryQueryRequest(
    val gameType: String,
    val matchType: String,
    val now: String,
    val todayStart: String
)
