package org.cxct.sportlottery.network.lottery

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AllWin(
    @Json(name = "prizeName")
    val prizeName: String,
    @Json(name = "prizeValue")
    val prizeValue: Int,
    @Json(name = "userName")
    val userName: String,
)