package org.cxct.sportlottery.network.chat.socketResponse.chatMessage

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers

/**
 * @author kevin
 * @create 2023/3/20
 * @description 推送来自红包雨中奖红包通知 chatType 1007
 */
@JsonClass(generateAdapter = true)
@KeepMembers
data class ChatWinRedEnvelopeResult(
    @Json(name = "userId")
    val userId: Long?,
    @Json(name = "nickName")
    val nickName: String?,
    @Json(name = "currency")
    val currency: String?,
    @Json(name = "money")
    val money: Double?,
    @Json(name = "userName")
    val userName: String?,
)
