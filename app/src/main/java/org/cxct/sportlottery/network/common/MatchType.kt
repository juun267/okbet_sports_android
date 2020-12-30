package org.cxct.sportlottery.network.common

enum class MatchType(val postValue: String) {
    IN_PLAY("INPLAY"),
    TODAY("TODAY"),
    EARLY("EARLY"),
    PARLAY("PARLAY"),
    AT_START("ATSTART")
}