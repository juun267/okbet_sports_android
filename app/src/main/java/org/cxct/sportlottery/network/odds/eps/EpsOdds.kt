package org.cxct.sportlottery.network.odds.eps

import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd


data class EpsOdds(
    var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    val matchInfo: MatchInfo?,
    val epsItem: Odd?,
    val isTitle:Boolean
)