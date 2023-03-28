package org.cxct.sportlottery.network.sport.coupon


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class SportCouponMenuData(
    @Json(name = "couponCode")
    val couponCode: String,
    @Json(name = "couponName")
    val couponName: String,
    @Json(name = "icon")
    val icon: String
)