package org.cxct.sportlottery.network.bet

import org.cxct.sportlottery.network.common.MatchType

data class Odd(
    val oid: String?,
    val odds: Double?,
    val stake: Double? = null,
    val oddsType: String
) {
    var matchType: MatchType? = null
}
