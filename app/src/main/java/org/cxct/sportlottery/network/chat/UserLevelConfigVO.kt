package org.cxct.sportlottery.network.chat

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.ui.chat.ChatMsgReceiveType

@JsonClass(generateAdapter = true)
data class UserLevelConfigVO(
    @Json(name = "name")
    val name: String?, //角色名称
    @Json(name = "bgColor")
    val bgColor: String?, //背景颜色
    @Json(name = "textColor")
    val textColor: String?, //文字颜色
    @Json(name = "betBgColor")
    val betBgColor: String?, //下注背景色
    @Json(name = "nickTextColor")
    val nickTextColor: String?, //昵称颜色
    @Json(name = "isDefault")
    val isDefault: String?, //是否系统默认 0否、1是
    @Json(name = "isKick")
    val isKick: String?, //是否允许踢人 0否、1是
    @Json(name = "isGag")
    val isGag: String?, //是否允许禁言 0否、1是
    @Json(name = "isSpeak")
    val isSpeak: String?, //是否允许发言 0否、1是
    @Json(name = "isChat")
    val isChat: String?, //是否允许私聊 0否、1是
    @Json(name = "isSendImg")
    val isSendImg: String?, //是否允许发送图片 0否、1是
    @Json(name = "isSendPacket")
    val isSendPacket: String?, //是否允许发红包 0否、1是
    @Json(name = "isSendAudio")
    val isSendAudio: String?, //是否允许发送语音 0否、1是
    @Json(name = "type")
    val type: String?, //类型 0游客、1会员、2管理員、3訪客
    @Json(name = "maxLength")
    val maxLength: Int?, //消息最大长度
    @Json(name = "platformId")
    val platformId: Int?,
    @Json(name = "code")
    val code: String?,
    override val itemType: Int = ChatMsgReceiveType.CHAT_UPDATE_USER_LEVEL_CONFIG,
): MultiItemEntity
