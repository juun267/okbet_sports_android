package org.cxct.sportlottery.network.odds.eps

import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd


data class EpsOdds(
    val matchInfo: MatchInfo? = null,
    val epsItem: Odd?
)