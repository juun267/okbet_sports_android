package org.cxct.sportlottery.network.bet.add

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class Stake(
    val parlayType: String,
    val stake: Double
)
