package org.cxct.sportlottery.net.chat.data


import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.chat.UserLevelConfigVO

@KeepMembers
data class GuestInitResponse (
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
    val state: Int, //state（0正常、1禁言、2禁止登录)
    val testFlag: Int,
    val token: String,
    val userId: Int,
    val userLevelId: Int,
    val userUniKey: String,
    val userLevelConfigVO: UserLevelConfigVO?,
)
