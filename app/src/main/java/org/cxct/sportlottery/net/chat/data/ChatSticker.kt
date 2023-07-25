package org.cxct.sportlottery.net.chat.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true)
@KeepMembers
data class ChatSticker (
    @Json(name = "type")
    val type: Int,
    @Json(name = "typeName")
    val typeName: String,
    @Json(name = "url")
    val url: String,
    @Json(name = "sort")
    val sort: Int,
    @Json(name = "platformId")
    val platformId: Int
    )