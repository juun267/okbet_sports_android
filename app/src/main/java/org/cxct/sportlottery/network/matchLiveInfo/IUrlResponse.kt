package org.cxct.sportlottery.network.matchLiveInfo


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class IUrlResponse(
    @Json(name = "hlsUrl")
    val hlsUrl: String?,
    @Json(name = "statusCode")
    val statusCode: Int
)