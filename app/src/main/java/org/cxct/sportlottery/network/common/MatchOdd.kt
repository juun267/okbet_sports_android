package org.cxct.sportlottery.network.common

import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.Odds
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.ui.common.PlayCateMapItem

interface MatchOdd {
    val matchInfo: MatchInfo?
    val oddsMap: MutableMap<String, MutableList<Odd?>?>
    val oddsSort: String?
    val quickPlayCateList: MutableList<QuickPlayCate>?
    val oddsEps: Odds?
    var playCateMappingList: List<PlayCateMapItem>?
}