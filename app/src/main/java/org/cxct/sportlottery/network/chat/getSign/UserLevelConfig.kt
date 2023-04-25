package org.cxct.sportlottery.network.chat.getSign


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserLevelConfig(
    @Json(name = "betRebate")
    val betRebate: Double,
    @Json(name = "code")
    val code: String,
    @Json(name = "growthMoney")
    val growthMoney: Double,
    @Json(name = "growthThreshold")
    val growthThreshold: Int,
    @Json(name = "id")
    val id: Int,
    @Json(name = "maxBetMoney")
    val maxBetMoney: Int,
    @Json(name = "maxCpBetMoney")
    val maxCpBetMoney: Int,
    @Json(name = "maxParlayBetMoney")
    val maxParlayBetMoney: Int,
    @Json(name = "maxSingleBetRebate")
    val maxSingleBetRebate: Double,
    @Json(name = "monthMoney")
    val monthMoney: Double,
    @Json(name = "name")
    val name: String,
    @Json(name = "platformCode")
    val platformCode: String,
    @Json(name = "platformId")
    val platformId: Int,
    @Json(name = "rechRebate")
    val rechRebate: Double,
    @Json(name = "remark")
    val remark: String,
    @Json(name = "updateDate")
    val updateDate: Long,
)