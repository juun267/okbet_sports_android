package org.cxct.sportlottery.network.matchCategory

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class MatchCategoryRequest(
    val startTime: Long, //今日开始时间（时间戳）
    val endTime: Long, //今日结束时间（时间戳）
    val gameType: String? = null, //运动类型 ex: FT, BK...
    val playCateMenuCode: String = "SPECIAL_MATCH_MOBILE" //PC端： SPECIAL_MATCH, mobile端：SPECIAL_MATCH_MOBILE
)


