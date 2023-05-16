package org.cxct.sportlottery.network.chat.socketResponse.chatMessage

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers


/**
 * @author Bill
 * @create 2023/3/14
 * @description
 * 个人讯息通知
 * chatType 1008 删除消息
 * */
@JsonClass(generateAdapter = true)
@KeepMembers
data class ChatRemoveMsgResult(
    @Json(name = "userId")
    val userId: Long,//	用户ID
    @Json(name = "messageId")
    val messageId: String?,//讯息ID
    @Json(name = "type")
    val type: Int?,//ChatType 聊天类型
    @Json(name = "userUniKey")
    val userUniKey: String?,//用户唯一识别码
    @Json(name = "nickName")
    val nickName: String?,//昵称
    @Json(name = "iconUrl")
    val iconUrl: String?,//头像
    @Json(name = "iconMiniUrl")
    val iconMiniUrl: String?,//头像缩图
    @Json(name = "remark")
    val remark: String?,//备注
    @Json(name = "content")
    val content: String?,//讯息内容
    @Json(name = "curTime")
    val curTime: String?,//讯息时间
)
