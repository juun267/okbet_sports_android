package org.cxct.sportlottery.network.service.user_notice

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class UserNotice(
    @Json(name = "addDate")
    val addDate: Long?,
    @Json(name = "content")
    val content: String?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "isRead")
    val isRead: Int?,
    @Json(name = "msgShowType")
    val msgShowType: Int?,
    @Json(name = "noticeType")
    val noticeType: Int?,
    @Json(name = "operatorId")
    val operatorId: Int?,
    @Json(name = "operatorName")
    val operatorName: String?,
    @Json(name = "platformId")
    val platformId: Int?,
    @Json(name = "tempId")
    val tempId: Int?,
    @Json(name = "title")
    val title: String?,
    @Json(name = "userId")
    val userId: Int?,
    @Json(name = "userName")
    val userName: String?
)