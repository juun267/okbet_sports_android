package org.cxct.sportlottery.network.sport.query


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SportQueryData(
    @Json(name = "items")
    val items: List<Item>?,
    @Json(name = "num")
    val num: Int?
)