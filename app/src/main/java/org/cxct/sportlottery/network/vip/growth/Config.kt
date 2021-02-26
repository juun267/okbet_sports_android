package org.cxct.sportlottery.network.vip.growth


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Config(
    @Json(name = "growthConfigs")
    val growthConfigs: List<GrowthConfig>,
    @Json(name = "userLevelConfigs")
    val userLevelConfigs: List<UserLevelConfig>
)