package org.cxct.sportlottery.network.matchTracker

import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class MatchTrackerUrl(
    val gameType: String?,
    val h5Url: String?,
    val mappingId: String?,
    val pcUrl: String?
)