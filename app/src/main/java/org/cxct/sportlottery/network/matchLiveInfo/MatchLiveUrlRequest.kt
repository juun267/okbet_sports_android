package org.cxct.sportlottery.network.matchLiveInfo


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchLiveUrlRequest(
    @Json(name = "isMobile")
    val isMobile: Int,
    @Json(name = "matchId")
    val matchId: String
)