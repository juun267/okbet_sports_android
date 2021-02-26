package org.cxct.sportlottery.network.vip.growth


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserLevelConfig(
    @Json(name = "code")
    val code: String,
    @Json(name = "growthThreshold")
    val growthThreshold: Int,
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String,
    @Json(name = "platformCode")
    val platformCode: String,
    @Json(name = "platformId")
    val platformId: Int,
    @Json(name = "remark")
    val remark: String,
    @Json(name = "updateDate")
    val updateDate: String
)