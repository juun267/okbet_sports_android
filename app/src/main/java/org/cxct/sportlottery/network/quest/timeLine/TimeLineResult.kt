package org.cxct.sportlottery.network.quest.timeLine

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@KeepMembers
data class TimeLineResult(
    override val code: Int,
    override val msg: String,
    override val success: Boolean,
    val t: TimeLine?
) : BaseResult()
