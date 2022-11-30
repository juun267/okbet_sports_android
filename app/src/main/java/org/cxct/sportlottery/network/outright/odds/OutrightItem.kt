package org.cxct.sportlottery.network.outright.odds

import org.cxct.sportlottery.network.odds.Odd

data class OutrightItem(
    val matchOdd: MatchOdd,
    val playCateCodeList: List<String>,
    val subTitleList: List<String>,
    var leagueExpanded: Boolean,
    val oddsList: List<List<Odd>>,
) {
    var collsePosition = mutableListOf<Int>()
}
