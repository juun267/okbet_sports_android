package org.cxct.sportlottery.network.matchLiveInfo


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class ChatLiveUserInfo(
    @Json(name = "userId")
    val userId: Long,
    @Json(name = "userUniKey")
    val userUniKey: String?,
    @Json(name = "nickName")
    val nickName: String?,
    @Json(name = "iconUrl")
    val iconUrl: String?,
    @Json(name = "iconMiniUrl")
    val iconMiniUrl: String?,
    @Json(name = "streamRole")
    val streamRole: String?,
    @Json(name = "frontCoverUrl")
    val frontCoverUrl: String?,
)