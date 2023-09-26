package org.cxct.sportlottery.net.chat.data

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class ChatSticker (
    val type: Int,
    val typeName: String,
    val url: String,
    val sort: Int,
    val platformId: Int
    )