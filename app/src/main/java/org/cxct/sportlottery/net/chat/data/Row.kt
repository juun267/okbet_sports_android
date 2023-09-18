package org.cxct.sportlottery.net.chat.data


import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class Row(
    val basicAmount: Int,
    val betMoney: Double,
    val constraintType: Int,
    val createDate: String,
    val id: Int,
    val isOpen: String,
    val isShowCount: String,
    val isSpeak: String,
    val language: String,
    val name: String,
    val onlineCount: Int,
    val platCode: String,
    val platName: String,
    val rechMoney: Double,
    val remark: String,
    val dataStatisticsRange: Int,
)