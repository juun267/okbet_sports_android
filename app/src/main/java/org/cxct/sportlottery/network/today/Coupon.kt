package org.cxct.sportlottery.network.today


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Coupon(
    @Json(name = "en")
    val en: Map<String, String>,
    @Json(name = "zh")
    val zh: Map<String, String>
)