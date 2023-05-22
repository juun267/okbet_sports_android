package org.cxct.sportlottery.network.bet.settledDetailList

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class RemarkBetRequest(
    val orderNo:String?,
    val reMark:String?,
    val addTime:String?
)
