package org.cxct.sportlottery.network.matchLiveInfo


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Response(
    @Json(name = "AccessToken")
    val accessToken: String?,
    @Json(name = "EventId")
    val eventId: String?,
    @Json(name = "HasCoverage")
    val hasCoverage: Boolean?,
    @Json(name = "HasLineup")
    val hasLineup: Boolean?,
    @Json(name = "LsEventId")
    val lsEventId: String?,
    @Json(name = "MatchTrackerEventId")
    val matchTrackerEventId: String?,
    @Json(name = "PartnerId")
    val partnerId: String?,
    @Json(name = "ProviderSportId")
    val providerSportId: String?,
    @Json(name = "StreamId")
    val streamId: String?,
    @Json(name = "StreamURL")
    val streamURL: String?,
    @Json(name = "StreamURLs")
    val StreamURLs: List<StreamURL>?,
    @Json(name = "VideoProvider")
    val videoProvider: String?,
    @Json(name = "ViewData")
    val viewData: Any?
)