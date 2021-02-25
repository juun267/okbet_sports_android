package org.cxct.sportlottery.network.vip.growth


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GrowthConfig(
    @Json(name = "dailyLimit")
    val dailyLimit: Int,
    @Json(name = "growth")
    val growth: Int,
    @Json(name = "id")
    val id: Int,
    @Json(name = "isLimit")
    val isLimit: Boolean,
    @Json(name = "platformId")
    val platformId: Int,
    @Json(name = "remark")
    val remark: String,
    @Json(name = "type")
    val type: String,
    @Json(name = "unit")
    val unit: Int
)