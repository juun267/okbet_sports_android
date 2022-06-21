package org.cxct.sportlottery.network.outright.odds

data class OutrightSubTitleItem(val belongMatchOdd: MatchOdd, val playCateCode: String, val subTitle: String, var leagueExpanded: Boolean)
