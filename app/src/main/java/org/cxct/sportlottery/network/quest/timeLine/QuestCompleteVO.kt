package org.cxct.sportlottery.network.quest.timeLine

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class QuestCompleteVO(
     val questId: Long,
     val questName: String,
     val timeType: Int?,
     val rewardId: Long,
     val rewardType: Long,
     val rewardValue: Double?,
     val deliverStatus: String?,
     val platformId: Long,
     val userId: Long,
     val currency: String,
)
