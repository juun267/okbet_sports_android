package org.cxct.sportlottery.network.chat.getUnPacket


import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class UnPacketRow(
    val id: Int,
    val roomId: Int,
    val currency: String,
    val rechMoney: Int,
    val betMoney: Int,
    val createBy: String,
    val createDate: Long,
    val status: Int,
    val packetType: Int,
    val platformId: Int,
)