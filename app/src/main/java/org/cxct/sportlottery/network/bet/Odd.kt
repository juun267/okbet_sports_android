package org.cxct.sportlottery.network.bet

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.MatchType

@KeepMembers
data class Odd(
    val oid: String?,
    val odds: Double?,
    val stake: Double? = null,
    val oddsType: String?,
    val spread: String?,
) {
    var matchType: MatchType? = null
}
