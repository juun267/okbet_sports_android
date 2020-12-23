package org.cxct.sportlottery.network.match

data class MatchPreloadRequest(
    val matchType: String,
    val num: Int? = null
)

enum class MatchType(val typeStr: String) {
    EARLY("EARLY"),
    INPLAY("INPLAY"),
    TODAY("TODAY")
}