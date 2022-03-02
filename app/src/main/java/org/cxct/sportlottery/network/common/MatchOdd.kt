package org.cxct.sportlottery.network.common

import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.ui.common.PlayCateMapItem

interface MatchOdd {
    val betPlayCateNameMap: Map<String?, Map<String?, String?>?>?
    val playCateNameMap: Map<String?, Map<String?, String?>?>?
    val matchInfo: MatchInfo?
    val oddsMap: MutableMap<String, MutableList<Odd?>?>?
    val oddsSort: String?
    val quickPlayCateList: MutableList<QuickPlayCate>?
    val oddsEps: EpsOdd?
    var playCateMappingList: List<PlayCateMapItem>?

}