package org.cxct.sportlottery.network.matchLiveInfo


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LaunchInfo(
    @Json(name = "streamLauncher")
    val streamLauncher: List<StreamLauncher>
)