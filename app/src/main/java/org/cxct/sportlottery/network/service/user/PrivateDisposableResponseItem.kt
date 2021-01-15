package org.cxct.sportlottery.network.service.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PrivateDisposableResponseItem(
    @Json(name = "eventType")
    val eventType: String?,
    @Json(name = "money")
    val money: Double?,
    @Json(name = "userNoticeList")
    val userNoticeList: List<UserNotice>? = listOf()
)