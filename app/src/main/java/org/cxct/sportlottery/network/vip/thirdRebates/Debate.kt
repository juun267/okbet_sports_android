package org.cxct.sportlottery.network.vip.thirdRebates


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Debate(
    @Json(name = "debate")
    val debate: Int,
    @Json(name = "firmCode")
    val firmCode: String,
    @Json(name = "firmType")
    val firmType: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "maxDebateMoney")
    val maxDebateMoney: Int,
    @Json(name = "maxMoney")
    val maxMoney: Int,
    @Json(name = "minMoney")
    val minMoney: Int,
    @Json(name = "platformId")
    val platformId: Int,
    @Json(name = "userLevelId")
    val userLevelId: Int,
    @Json(name = "userLevelName")
    val userLevelName: String
)