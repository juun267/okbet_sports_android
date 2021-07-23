package org.cxct.sportlottery.network.matchCategory

//是否为行动装置 0: 否，1: 是
data class MatchRecommendRequest(val isMobile: Int = 1)