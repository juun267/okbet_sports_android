package org.cxct.sportlottery.network.sport.query

data class SportQueryRequest(
    val now: String,
    val todayStart: String,
    val matchType: String,
    val matchIdList: List<String>? = null,
    val leagueIdList: List<String>? = null
)