package org.cxct.sportlottery.network.matchCategory

import org.cxct.sportlottery.common.proguards.KeepMembers

//是否为行动装置 0: 否，1: 是
@KeepMembers
data class MatchRecommendRequest(val isMobile: Int = 1)