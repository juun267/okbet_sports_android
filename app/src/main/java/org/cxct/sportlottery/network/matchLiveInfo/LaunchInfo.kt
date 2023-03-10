package org.cxct.sportlottery.network.matchLiveInfo


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class LaunchInfo(
    @Json(name = "streamLauncher")
    val streamLauncher: List<StreamLauncher>
)