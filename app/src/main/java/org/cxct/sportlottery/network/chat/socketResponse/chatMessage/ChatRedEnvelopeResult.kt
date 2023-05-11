package org.cxct.sportlottery.network.chat.socketResponse.chatMessage

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers

/**
 * @author Bill
 * @create 2023/3/14
 * @description
 * 聊天室聊天訊息
 * chatType 1001 发送红包
 * */

@JsonClass(generateAdapter = true)
@KeepMembers
data class ChatRedEnvelopeResult(
    @Json(name = "id")
    val id: Long,//红包id
    @Json(name = "currency")
    val currency: String,//币别
    @Json(name = "totalNum")
    val totalNum: Int?,//红包总个数
    @Json(name = "status")
    val status: Int?,//红包状态（1：正常；2：已抢完；3：已关闭）
    @Json(name = "packetType")
    val packetType: Int,//0-立刻发红包(系统红包) 1每日红包（暂无） 2随机红包（暂无） 3定向红包（暂无）
    @Json(name = "nickName")
    val nickName: String?,
)
