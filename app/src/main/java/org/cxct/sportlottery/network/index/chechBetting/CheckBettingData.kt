package org.cxct.sportlottery.network.index.chechBetting

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class CheckBettingData(
    @Json(name = "id")
    val id: String? = null,
    @Json(name = "code")
    val code: String? = null,
    @Json(name = "name")
    val name: String? = null
)