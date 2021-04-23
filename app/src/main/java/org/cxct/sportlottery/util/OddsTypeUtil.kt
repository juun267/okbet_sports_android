package org.cxct.sportlottery.util


import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.ui.menu.OddsType


fun getOdds(odd: Odd, oddsType: OddsType): Double {
    return when (oddsType) {
        OddsType.EU -> odd.odds ?: 0.0
        OddsType.HK -> odd.hkOdds ?: 0.0
    }
}


fun getOdds(matchOdd: MatchOdd, oddsType: OddsType): Double {
    return when (oddsType) {
        OddsType.EU -> matchOdd.odds
        OddsType.HK -> matchOdd.hkOdds
    }
}


fun getOdds(matchOdd: org.cxct.sportlottery.network.bet.MatchOdd, oddsType: OddsType): Double {
    return when (oddsType) {
        OddsType.EU -> matchOdd.odds
        OddsType.HK -> matchOdd.hkOdds
    }
}


fun getOdds(parlayOdd: ParlayOdd, oddsType: OddsType): Double {
    return when (oddsType) {
        OddsType.EU -> parlayOdd.odds
        OddsType.HK -> parlayOdd.hkOdds?:0.0
    }
}


