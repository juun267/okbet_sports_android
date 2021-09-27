package org.cxct.sportlottery.network.common

import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.Odds
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.ui.common.PlayCateMapItem

interface MatchOdd {
    val matchInfo: MatchInfo?
    var oddsMap: MutableMap<String, MutableList<Odd?>?>
    val oddsSort: String?
    var quickPlayCateList: List<QuickPlayCate>?
    val oddsEps: Odds?
    var playCateMappingList: List<PlayCateMapItem>?
}