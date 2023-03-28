package org.cxct.sportlottery.network.sport.publicityRecommend


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class PublicityRecommendRequest(
    @Json(name = "now")
    val now: String,
    @Json(name = "todayStart")
    val todayStart: String
)