package org.cxct.sportlottery.network.sport.query


import com.squareup.moshi.Json

data class Item(
    @Json(name = "code")
    val code: String?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "num")
    val num: Int?,
    @Json(name = "play")
    val play: List<Play>?,
    @Json(name = "sortNum")
    val sortNum: Int?
)