package org.cxct.sportlottery.network.matchLiveInfo


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class StreamURL(
    @Json(name = "url")
    val url: String?,
    @Json(name = "format")
    val format: String?,
    @Json(name = "type")
    val type: Int?,
    @Json(name = "status")
    val status: Int?
)