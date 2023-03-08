package org.cxct.sportlottery.network.matchTracker

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchTrackerUrl(
    val gameType: String?,
    val h5Url: String?,
    val mappingId: String?,
    val pcUrl: String?
)