package org.cxct.sportlottery.network.matchLiveInfo


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true)
@KeepMembers
data class StreamLauncher(
    @Json(name = "audioLang")
    val audioLang: String,
    @Json(name = "launcherURL")
    val launcherURL: String,
    @Json(name = "playerAlias")
    val playerAlias: String
)