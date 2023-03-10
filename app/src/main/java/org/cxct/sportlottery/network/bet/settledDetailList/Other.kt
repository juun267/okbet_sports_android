package org.cxct.sportlottery.network.bet.settledDetailList


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Other (
    @Json(name = "totalAmount")
    val totalAmount: Double?,
    @Json(name = "win")
    val win: Double?,
    @Json(name = "valueBetAmount")
    val valueBetAmount:Double?
)