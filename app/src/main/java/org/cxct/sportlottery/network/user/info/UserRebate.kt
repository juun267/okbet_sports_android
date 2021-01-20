package org.cxct.sportlottery.network.user.info

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserRebate(
    @Json(name = "gameCate")
    val gameCate: String,
    @Json(name = "cateId")
    val cateId: Int,
    @Json(name = "rebate")
    val rebate: Int,
)
