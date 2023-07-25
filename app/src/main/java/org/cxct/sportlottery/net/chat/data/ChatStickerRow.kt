package org.cxct.sportlottery.net.chat.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true)
@KeepMembers
data class ChatStickerRow (
    @Json(name = "typeName")
    val typeName: String,
    @Json(name = "list")
    val list: ArrayList<ChatSticker>,
    var select:Boolean=false
    )