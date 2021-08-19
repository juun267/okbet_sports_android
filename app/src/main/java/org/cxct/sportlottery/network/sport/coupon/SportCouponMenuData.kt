package org.cxct.sportlottery.network.sport.coupon


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SportCouponMenuData(
    @Json(name = "couponCode")
    val couponCode: String,
    @Json(name = "couponName")
    val couponName: String,
    @Json(name = "icon")
    val icon: String
)