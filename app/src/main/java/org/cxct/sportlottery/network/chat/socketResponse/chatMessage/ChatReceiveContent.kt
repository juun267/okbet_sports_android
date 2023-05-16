package org.cxct.sportlottery.network.chat.socketResponse.chatMessage


import org.cxct.sportlottery.common.proguards.KeepMembers


@KeepMembers
data class ChatReceiveContent<T> (
    val content: T?,
    val msg: String?,
    val seq: Int = 0,//信息编号,client发送讯息id,如果为server主动推送，则为0
    val time: Long,
    val type: Int = -1
){

    @Suppress("UNCHECKED_CAST")
    fun <T> getThisContent(): T? {
        return content as T?
    }

    var isMySelf = false //自己的訊息

    var isCustomMessage = false //客製化訊息(介面需求而新增的訊息)
}