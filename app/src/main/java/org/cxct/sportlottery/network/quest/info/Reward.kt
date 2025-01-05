package org.cxct.sportlottery.network.quest.info

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class Reward(
    val questId: Long?,
    val questName: String?,
    val rewardId: Long?,
    val recordId: Long?,
    val rewardType: Long?,
    val rewardValue: Double?,
    val deliverStatus: String?,
    val expiredDate: Long?
) {
    /**
     * 任务奖励类别Enum
     * @see RewardType
     */
    val rewardTypeEnum: RewardType? = RewardType.toEnum(rewardType)
}
