package org.cxct.sportlottery.network.quest.claimAllReward

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class ClaimAllRewardRequest(val rewardIds: List<Long>)
