package org.cxct.sportlottery.network.chat.socketResponse.chatMessage


import com.chad.library.adapter.base.entity.MultiItemEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.ui.chat.bean.ChatMessage


@KeepMembers
data class ChatReceiveContent<T> (
    val content: T?,
    val msg: String?,
    val seq: Int?,//信息编号,client发送讯息id,如果为server主动推送，则为0
    val time: Long,
    val type: Int?
): MultiItemEntity {

    override val itemType: Int = 0

    @Suppress("UNCHECKED_CAST")
    fun <T> getThisContent(): T? {
        return content as T?
    }

    var isMySelf = false //自己的訊息

    var isCustomMessage = false //客製化訊息(介面需求而新增的訊息)
}