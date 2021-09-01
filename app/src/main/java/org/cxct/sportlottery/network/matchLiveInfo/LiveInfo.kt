package org.cxct.sportlottery.network.matchLiveInfo


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LiveInfo(
    @Json(name = "accessToken")
    val accessToken: String,
    @Json(name = "eventId")
    val eventId: String,
    @Json(name = "lsEventId")
    val lsEventId: String,
    @Json(name = "matchTrackerId")
    val matchTrackerId: String,
    @Json(name = "providerSportId")
    val providerSportId: String,
    @Json(name = "streamURL")
    val streamURL: String,
    @Json(name = "tvArea")
    val tvArea: String,
    @Json(name = "videoProvider")
    val videoProvider: String,
    @Json(name = "viewData")
    val viewData: String
)