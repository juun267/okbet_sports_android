package org.cxct.sportlottery.network.sport.query


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class SportQueryData(
    @Json(name = "items")
    val items: List<Item>?,
    @Json(name = "num")
    val num: Int?
)