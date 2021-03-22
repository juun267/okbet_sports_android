package org.cxct.sportlottery.network.vip.growth


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GrowthConfig(
    @Json(name = "id")
    val id: Int,
    @Json(name = "platformId")
    val platformId: Int,
    @Json(name = "type")
    val type: String,
    @Json(name = "unit")
    val unit: Int,
    @Json(name = "growth")
    val growth: Int,
    @Json(name = "isLimit")
    val isLimit: Boolean,
    @Json(name = "remark")
    val remark: String,
)

const val GROWTH_CONFIG_RECHARGE_ID = 11
const val GROWTH_CONFIG_BET_ID = 12