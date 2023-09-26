package org.cxct.sportlottery.net.chat.data

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class ChatStickerRow (
    val typeName: String,
    val list: ArrayList<ChatSticker>,
    var select:Boolean=false
    )