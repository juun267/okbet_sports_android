package org.cxct.sportlottery.network.matchLiveInfo


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IUrlResponse(
    @Json(name = "hlsUrl")
    val hlsUrl: String,
    @Json(name = "statusCode")
    val statusCode: Int
)