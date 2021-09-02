package org.cxct.sportlottery.network.common

import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate

interface MatchOdd {
    val matchInfo: MatchInfo?
    var odds: MutableMap<String, MutableList<Odd?>>
    val quickPlayCateList: List<QuickPlayCate>?
}