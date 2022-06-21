package org.cxct.sportlottery.network.outright.odds

data class OutrightShowMoreItem(val playCateCode: String, val matchOdd: MatchOdd, var playCateExpand: Boolean, var isExpanded: Boolean, var leagueExpanded: Boolean)
