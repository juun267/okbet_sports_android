package org.cxct.sportlottery.network.sport.query


import com.squareup.moshi.Json

data class SportQueryData(
    @Json(name = "items")
    val items: List<Item>?,
    @Json(name = "num")
    val num: Int?
)