package org.cxct.sportlottery.network.match


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchRound(
    @Json(name = "pullRtmpUrl")
    val pullRtmpUrl: String,
    @Json(name = "pullFlvUrl")
    val pullFlvUrl: String,
    @Json(name = "frontCoverUrl")
    val frontCoverUrl: String,
) {
    var roundNo: String = ""

}