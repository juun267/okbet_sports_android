package org.cxct.sportlottery.net.point.data

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class PointRule(
    val rechMoney: Int,
    val rechPoint: Int,
    val rechStatus: Int,
    val validBet: Int,
    val validBetPoint: Int,
    val validBetStatus: Int
)