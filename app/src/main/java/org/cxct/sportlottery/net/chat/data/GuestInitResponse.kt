package org.cxct.sportlottery.net.chat.data


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.chat.UserLevelConfigVO

@JsonClass(generateAdapter = true)
@KeepMembers
data class GuestInitResponse (
    @Json(name = "betMoney")
    val betMoney: Int,
    @Json(name = "currency")
    val currency: String,
    @Json(name = "iconMiniUrl")
    val iconMiniUrl: String,
    @Json(name = "iconUrl")
    val iconUrl: String,
    @Json(name = "isDefaultIcon")
    val isDefaultIcon: String,
    @Json(name = "lang")
    val lang: String,
    @Json(name = "lastMessageTime")
    val lastMessageTime: String,
    @Json(name = "nationCode")
    val nationCode: String,
    @Json(name = "nickName")
    val nickName: String,
    @Json(name = "platformId")
    val platformId: Int,
    @Json(name = "rechMoney")
    val rechMoney: Int,
    @Json(name = "state")
    val state: Int, //state（0正常、1禁言、2禁止登录)
    @Json(name = "testFlag")
    val testFlag: Int,
    @Json(name = "token")
    val token: String,
    @Json(name = "userId")
    val userId: Int,
    @Json(name = "userLevelId")
    val userLevelId: Int,
    @Json(name = "userUniKey")
    val userUniKey: String,
    @Json(name = "userLevelConfigVO")
    val userLevelConfigVO: UserLevelConfigVO?,
)
