package org.cxct.sportlottery.network.vip.growth


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class UserLevelConfig(
    @Json(name = "growthThreshold")
    val growthThreshold: Int,
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String,
)