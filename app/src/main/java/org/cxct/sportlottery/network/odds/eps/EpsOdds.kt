package org.cxct.sportlottery.network.odds.eps

import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd


data class EpsOdds(
    val betPlayCateNameMap: Map<String?, Map<String?, String?>?>?,
    val matchInfo: MatchInfo?,
    val epsItem: Odd?,
    val isTitle:Boolean
)