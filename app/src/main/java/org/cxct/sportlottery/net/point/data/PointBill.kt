package org.cxct.sportlottery.net.point.data

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class PointBill(
    val addTime: Long,
    val points: Int,
    val reason: String,
    val remark: String,
    val type: Int=1,
)