package org.cxct.sportlottery.ui.bet.list

import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType

class BetInfoListData(
    val matchOdd: MatchOdd,
    val parlayOdds: ParlayOdd?
) {
    var matchType: MatchType? = null
    var input: String? = null
    var oddsHasChanged = false
    var betAmount: Double = 0.0
    var pointMarked: Boolean = false //紅色標記, 紀錄是否能串關
}

