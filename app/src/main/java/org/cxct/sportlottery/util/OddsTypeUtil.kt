package org.cxct.sportlottery.util


import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.menu.OddsType


fun getOdds(odd: Odd?, oddsType: OddsType): Double {
    return when (oddsType) {
        OddsType.EU -> odd?.odds ?: 0.0
        OddsType.HK -> odd?.hkOdds ?: 0.0
        //Martin
        OddsType.MYS -> odd?.malayOdds ?: 0.0
        OddsType.IDN -> odd?.indoOdds ?: 0.0
    }
}


fun getOdds(matchOdd: MatchOdd?, oddsType: OddsType): Double {
    return when (oddsType) {
        OddsType.EU -> matchOdd?.odds ?: 0.0
        OddsType.HK -> matchOdd?.hkOdds ?: 0.0
        //Martin
        OddsType.MYS -> matchOdd?.malayOdds ?: 0.0
        OddsType.IDN -> matchOdd?.indoOdds ?: 0.0
    }
}

fun getOddsNew(matchOdd: MatchOdd?, oddsType: OddsType): Double {
    var currentOddsType = oddsType
    if (matchOdd?.odds == matchOdd?.malayOdds) {
        currentOddsType = OddsType.EU
    }
    return when (currentOddsType) {
        OddsType.EU -> matchOdd?.odds ?: 0.0
        OddsType.HK -> matchOdd?.hkOdds ?: 0.0
        //Martin
        OddsType.MYS -> matchOdd?.malayOdds ?: 0.0
        OddsType.IDN -> matchOdd?.indoOdds ?: 0.0
    }
}

fun getOdds(
    matchOdd: org.cxct.sportlottery.network.bet.settledDetailList.MatchOdd?,
    oddsType: String
): Double {
    return when (oddsType) {
        OddsType.EU.code -> matchOdd?.odds ?: 0.0
        OddsType.HK.code -> matchOdd?.hkOdds ?: 0.0
        //Martin
        OddsType.MYS.code -> matchOdd?.malayOdds ?: 0.0
        OddsType.IDN.code -> matchOdd?.indoOdds ?: 0.0
        else -> 0.0
    }
}

fun getOdds(
    matchOdd: org.cxct.sportlottery.network.bet.add.betReceipt.MatchOdd?,
    oddsType: OddsType
): Double {
    return when (oddsType) {
        OddsType.EU -> matchOdd?.odds ?: 0.0
        OddsType.HK -> matchOdd?.hkOdds ?: 0.0
        //Martin
        OddsType.MYS -> matchOdd?.malayOdds ?: 0.0
        OddsType.IDN -> matchOdd?.indoOdds ?: 0.0
    }
}


fun getOdds(matchOdd: org.cxct.sportlottery.network.bet.MatchOdd, oddsType: OddsType): Double? {
    return when (oddsType) {
        OddsType.EU -> matchOdd.odds
        OddsType.HK -> matchOdd.hkOdds
        //Martin
        OddsType.MYS -> matchOdd?.malayOdds
        OddsType.IDN -> matchOdd?.indoOdds
    }
}


fun getOdds(parlayOdd: ParlayOdd, oddsType: OddsType): Double {
    return when (oddsType) {
        OddsType.EU -> parlayOdd.odds
        OddsType.HK -> parlayOdd.hkOdds ?: 0.0
        //Martin
        OddsType.MYS -> parlayOdd.malayOdds ?: 0.0
        OddsType.IDN -> parlayOdd.indoOdds ?: 0.0
    }
}

fun getOddTypeRes(
    matchOdd: org.cxct.sportlottery.network.bet.add.betReceipt.MatchOdd,
    oddsType: OddsType
): Int {
    val sameValue = mutableSetOf<Double>()
    sameValue.add(matchOdd.odds ?: 0.0)
    sameValue.add(matchOdd.hkOdds ?: 0.0)
    sameValue.add(matchOdd.malayOdds ?: 0.0)
    sameValue.add(matchOdd.indoOdds ?: 0.0)

    return if (sameValue.size > 1) {
        OddsType.EU.res
    } else {
        oddsType.res
    }
}


