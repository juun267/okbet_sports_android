package org.cxct.sportlottery.network.today


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchCategoryQueryDesc(
    @Json(name = "coupon")
    val coupon: Coupon
)