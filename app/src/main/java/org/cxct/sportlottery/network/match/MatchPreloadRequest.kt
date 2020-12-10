package org.cxct.sportlottery.network.match

data class MatchPreloadRequest(
    val matchType: String,
    val num: Int? = null
)