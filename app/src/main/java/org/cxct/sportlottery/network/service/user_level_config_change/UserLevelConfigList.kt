package org.cxct.sportlottery.network.service.user_level_config_change

import com.squareup.moshi.Json
import org.cxct.sportlottery.common.proguard.KeepMembers

@KeepMembers
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
    val maxBetMoney: Long,
    @Json(name = "maxParlayBetMoney")
    val maxParlayBetMoney: Long,
    @Json(name = "maxCpBetMoney")
    val maxCpBetMoney: Long
)
