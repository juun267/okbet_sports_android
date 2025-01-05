package org.cxct.sportlottery.network.quest.timeLine

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class TimeLine(
    val betWinNotices: Any?,
    val gameWinMoney: Double?,
    val lastTurnNum: Int?,
    val pixelVOList: List<Any>,
    val questCompleteVOList: List<QuestCompleteVO>?,
    val redEnpRainVO: Double?,
    val serverTime: Long?,
    val unsettleMoney: Double?,
    val userMoney: Double?,
    val userNotices: Any?
)