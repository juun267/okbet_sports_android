package org.cxct.sportlottery.network.bet

import org.cxct.sportlottery.network.common.MatchType

data class Odd(
    val oid: String?,
    val odds: Double?
) {
    var matchType: MatchType? = null
}
