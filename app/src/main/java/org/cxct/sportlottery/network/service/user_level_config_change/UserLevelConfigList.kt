package org.cxct.sportlottery.network.service.user_level_config_change

import com.squareup.moshi.Json

data class UserLevelConfigList(
    @Json(name = "id")
    val id: Int,
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "growthThreshold")
    val growthThreshold: Int,
    @Json(name = "maxBetMoney")
    val maxBetMoney: Int,
    @Json(name = "maxParlayBetMoney")
    val maxParlayBetMoney: Int,
    @Json(name = "maxCpBetMoney")
    val maxCpBetMoney: Int
)
