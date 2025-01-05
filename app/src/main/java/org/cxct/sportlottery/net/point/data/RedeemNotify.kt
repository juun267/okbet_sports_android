package org.cxct.sportlottery.net.point.data

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class RedeemNotify(
    val count: Int,
    val productName: String,
    val userName: String
)