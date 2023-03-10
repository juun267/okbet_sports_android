package org.cxct.sportlottery.network.matchLiveInfo


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Meta(
    @Json(name = "accessLevel")
    val accessLevel: String,
    @Json(name = "id")
    val id: String
)