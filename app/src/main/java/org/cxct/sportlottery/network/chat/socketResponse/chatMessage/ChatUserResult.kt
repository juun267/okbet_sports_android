package org.cxct.sportlottery.network.chat.socketResponse.chatMessage

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers


/**
 * @author Bill
 * @create 2023/3/14
 * @description
 * 聊天用户响应格式
 * chatType 1002 用户进入房间
 * chatType 1003 用户离开房间
 * */
@JsonClass(generateAdapter = true)
@KeepMembers
data class ChatUserResult(
    @Json(name = "userId")
    val userId: Long,//	用户ID
    @Json(name = "userUniKey")
    val userUniKey: String,//用户唯一识别码
    @Json(name = "nickName")
    val nickName: String?,//昵称
    @Json(name = "iconUrl")
    val iconUrl: String?,//头像
    @Json(name = "iconMiniUrl")
    val iconMiniUrl: String?,//头像缩图
    @Json(name = "platformId")
    val platformId: Long?,//平台id
    @Json(name = "userLevelId")
    val userLevelId: Long?,//用户等级
    @Json(name = "currency")
    val currency: String?,//币别
    @Json(name = "nationCode")
    val nationCode: String?,//国家
    @Json(name = "rechMoney")
    val rechMoney: Double?,//近2天充值
    @Json(name = "betMoney")
    val betMoney: Double?,//近2天下注
    @Json(name = "state")
    val state: Int?,//用户状态（0正常、1禁言、2禁止登录）
    @Json(name = "testFlag")
    val testFlag: Int?,//测试试玩账号类型：0-普通账号，2-内部测试账号，1-游客
    @Json(name = "lang")
    val lang: String?,//语系
    @Json(name = "isDefaultIcon")
    val isDefaultIcon: String?,//是否使用预设头像 0:否 1:是
    @Json(name = "lastMessageTime")
    val lastMessageTime: Long?,//最后一次发言时间
    @Json(name = "token")
    val token: String?,
)
