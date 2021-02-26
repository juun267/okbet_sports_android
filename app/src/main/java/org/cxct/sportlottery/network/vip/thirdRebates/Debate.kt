package org.cxct.sportlottery.network.vip.thirdRebates


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Debate(
    @Json(name = "debate")
    val debate: Double?,
    @Json(name = "firmCode")
    val firmCode: String,
    @Json(name = "firmType")
    val firmType: String,
    @Json(name = "id")
    val id: Double,
    @Json(name = "maxDebateMoney")
    val maxDebateMoney: Double?,
    @Json(name = "maxMoney")
    val maxMoney: Double?,
    @Json(name = "minMoney")
    val minMoney: Double?,
    @Json(name = "platformId")
    val platformId: Long,
    @Json(name = "userLevelId")
    val userLevelId: Int,
    @Json(name = "userLevelName")
    val userLevelName: String
)