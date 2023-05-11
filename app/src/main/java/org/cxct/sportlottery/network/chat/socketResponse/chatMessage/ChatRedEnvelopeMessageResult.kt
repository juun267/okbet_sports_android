package org.cxct.sportlottery.network.chat.socketResponse.chatMessage

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers

/**
 * @author Kevin
 * @create 2023/3/16
 * @description
 * 聊天室聊天訊息
 * chatType 房间用户红包消息 2008
 * */

@JsonClass(generateAdapter = true)
@KeepMembers
data class ChatRedEnvelopeMessageResult(
    @Json(name = "id")
    val id: Int?,
    @Json(name = "roomId")
    val roomId: Int?,
    @Json(name = "currency")
    val currency: String?,
    @Json(name = "rechMoney")
    val rechMoney: Int?,
    @Json(name = "betMoney")
    val betMoney: Int?,
    @Json(name = "createBy")
    val createBy: String?,
    @Json(name = "createDate")
    val createDate: String?,
    @Json(name = "status")
    val status: String?,
    @Json(name = "packetType")
    val packetType: Int?,
    @Json(name = "platformId")
    val platformId: Int?,
    @Json(name = "nickName")
    val nickName: String?,
)
