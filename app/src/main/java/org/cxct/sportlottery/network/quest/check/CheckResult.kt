package org.cxct.sportlottery.network.quest.check

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@KeepMembers
data class CheckResult(
    override val code: Int,
    override val msg: String,
    override val success: Boolean
) : BaseResult()
