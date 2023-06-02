package org.cxct.sportlottery.net.chat.data

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class JoinRoomResonse(
    val betMoney: Int,
    val currency: String,
    val iconMiniUrl: String,
    val iconUrl: String,
    val isDefaultIcon: String,
    val lang: String,
    val lastMessageTime: String,
    val nationCode: String,
    val nickName: String,
    val platformId: Int,
    val rechMoney: Int,
    val state: Int,
    val testFlag: Int,
    val token: String,
    val userId: Int,
    val userLevelId: Int,
    val userUniKey: String,
)