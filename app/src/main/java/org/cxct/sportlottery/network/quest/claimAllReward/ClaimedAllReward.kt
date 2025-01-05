package org.cxct.sportlottery.network.quest.claimAllReward

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class ClaimedAllReward(
    val successRewardIds: List<Long?>?,
    val failedRewardIds: List<Long?>?
)