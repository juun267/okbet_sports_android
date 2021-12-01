package org.cxct.sportlottery.network.vip.growth


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserLevelConfig(
    @Json(name = "growthThreshold")
    val growthThreshold: Int,
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String,
)