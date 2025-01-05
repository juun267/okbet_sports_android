package org.cxct.sportlottery.net.point.data

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class ExpiredPoints(
    val expiredTime: Long?,
    val points: Double?
)