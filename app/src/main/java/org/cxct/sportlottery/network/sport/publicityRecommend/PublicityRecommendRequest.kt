package org.cxct.sportlottery.network.sport.publicityRecommend


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PublicityRecommendRequest(
    @Json(name = "now")
    val now: String,
    @Json(name = "todayStart")
    val todayStart: String
)