package org.cxct.sportlottery.network.chat.socketResponse.chatMessage

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers

/**
 * @author Bill
 * @create 2023/3/14
 * @description
 * 个人讯息通知
 * chatType 9999 异常信息 CODE
 * */
@JsonClass(generateAdapter = true)
@KeepMembers
data class ChatErrorResult(
    @Json(name = "message")
    val message: String?,//	错误讯息
)