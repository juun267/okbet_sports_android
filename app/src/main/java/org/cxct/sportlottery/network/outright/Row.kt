package org.cxct.sportlottery.network.outright


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Row(
    @Json(name = "resultList")
    val resultList: List<Result>,
    @Json(name = "season")
    val season: Season
)