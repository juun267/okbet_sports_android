package org.cxct.sportlottery.network.lottery

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Win(
    @Json(name = "drawTime")
    val drawTime: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "imageUrl")
    val imageUrl: String,
    @Json(name = "priceValue")
    val priceValue: Int,
    @Json(name = "prizeStatus")
    val prizeStatus: Int,
)