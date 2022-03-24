package org.cxct.sportlottery.network.common

import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.ui.common.PlayCateMapItem

interface MatchOdd {
    var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?
    var playCateNameMap: MutableMap<String?, Map<String?, String?>?>?
    val matchInfo: MatchInfo?
    var oddsMap: MutableMap<String, MutableList<Odd?>?>?
    val oddsSort: String?
    val quickPlayCateList: MutableList<QuickPlayCate>?
    val oddsEps: EpsOdd?
    var playCateMappingList: List<PlayCateMapItem>?

}