package org.cxct.sportlottery.network.sport.query

data class SearchRequest(
    val now: String,
    val todayStart: String
)