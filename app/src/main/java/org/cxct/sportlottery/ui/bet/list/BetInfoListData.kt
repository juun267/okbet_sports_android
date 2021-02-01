package org.cxct.sportlottery.ui.bet.list

import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType

class BetInfoListData (
    val matchOdd: MatchOdd,
    val parlayOdds: ParlayOdd
){
    var matchType: MatchType? = null
}

