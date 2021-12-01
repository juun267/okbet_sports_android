package org.cxct.sportlottery.network.matchLiveInfo


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchLiveInfoResponse(
    @Json(name = "IsSuccess")
    val isSuccess: Boolean?,
    @Json(name = "Message")
    val message: String?,
    @Json(name = "Response")
    val response: Response?,
    @Json(name = "ReturnCode")
    val returnCode: String?,
    @Json(name = "SysDateTime")
    val sysDateTime: String?
)