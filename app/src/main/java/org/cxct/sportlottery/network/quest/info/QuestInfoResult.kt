package org.cxct.sportlottery.network.quest.info

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@KeepMembers
data class QuestInfoResult(
    override val code: Int,
    override val msg: String,
    override val success: Boolean,
    val t: QuestInfo?
) : BaseResult()