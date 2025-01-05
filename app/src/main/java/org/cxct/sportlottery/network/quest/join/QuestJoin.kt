package org.cxct.sportlottery.network.quest.join

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class QuestJoin(
    val deliverStatus: Long?,
    val recordId: Long?,
    val rewardId: Long?,
    val status: Long?
)