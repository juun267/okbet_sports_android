package org.cxct.sportlottery.network.money.config

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class RechSort(
    val name: String,
    val onlineType: Int,
    val sort: Int=0
)