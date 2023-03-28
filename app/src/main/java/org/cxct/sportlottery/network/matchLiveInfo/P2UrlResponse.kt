package org.cxct.sportlottery.network.matchLiveInfo


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class P2UrlResponse(
    @Json(name = "launchInfo")
    val launchInfo: LaunchInfo,
    @Json(name = "meta")
    val meta: Meta,
    @Json(name = "sessionToken")
    val sessionToken: String
)