package org.cxct.sportlottery.network.vip.growth


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Config(
    @Json(name = "GrowthConfigs")
    val growthConfigs: List<GrowthConfig>,
    @Json(name = "UserLevelConfigs")
    val userLevelConfigs: List<UserLevelConfig>
)