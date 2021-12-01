package org.cxct.sportlottery.network.matchLiveInfo


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class P2UrlResponse(
    @Json(name = "launchInfo")
    val launchInfo: LaunchInfo,
    @Json(name = "meta")
    val meta: Meta,
    @Json(name = "sessionToken")
    val sessionToken: String
)