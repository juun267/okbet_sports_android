package org.cxct.sportlottery.network.chat.socketResponse.chatMessage


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatReceiveContent<T>(
    @Json(name = "content")
    val content: T?,
    @Json(name = "msg")
    val msg: String?,
    @Json(name = "seq")
    val seq: Int?,//信息编号,client发送讯息id,如果为server主动推送，则为0
    @Json(name = "time")
    val time: Long?,
    @Json(name = "type")
    val type: Int?,
) {
    @Suppress("UNCHECKED_CAST")
    fun <T> getThisContent(): T? {
        return content as T?
    }

    var isMySelf = false //自己的訊息

    var isCustomMessage = false //客製化訊息(介面需求而新增的訊息)
}