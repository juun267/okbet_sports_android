package org.cxct.sportlottery.network.bet.settledDetailList


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Other (
    @Json(name = "totalAmount")
    val totalAmount: Long?,
    @Json(name = "win")
    val win: Double?,
    @Json(name = "valueBetAmount")
    val valueBetAmount:Int?
)