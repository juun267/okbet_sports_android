package org.cxct.sportlottery.network.bet.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Other (
    @Json(name = "totalAmount")
    val totalAmount: Double?,
    @Json(name = "win")
    val win: Double?,
    @Json(name = "valueBetAmount")
    val valueBetAmount:Double?,
    @Json(name = "userPlayAmount")
    val userPlayAmount:Double?,
    @Json(name = "newBkEndTotalAmount")
    val newBkEndTotalAmount:Double?,
    @Json(name = "newBKEndNetWin")
    val newBKEndNetWin:Double?
)