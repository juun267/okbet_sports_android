package org.cxct.sportlottery.net.point.data

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class ProductCateVO(
    val id: Int,
    val name: String
)