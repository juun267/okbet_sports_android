package org.cxct.sportlottery.util

import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.ui.menu.OddsType


fun getOdds(matchOdd: MatchOdd, oddsType: String): Double {
    return when (oddsType) {
        OddsType.EU.value -> matchOdd.odds
        OddsType.HK.value -> matchOdd.hkOdds
        else -> 0.0
    }
}


fun getOdds(parlayOdd: ParlayOdd, oddsType: String): Double {
    return when (oddsType) {
        OddsType.EU.value -> parlayOdd.odds
        OddsType.HK.value -> parlayOdd.hkOdds
        else -> 0.0
    }
}
