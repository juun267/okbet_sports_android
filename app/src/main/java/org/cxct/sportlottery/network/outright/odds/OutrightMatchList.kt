package org.cxct.sportlottery.network.outright.odds

data class OutrightMatchList(
    val matchOdds: List<MatchOdd?> ?= listOf()
)
