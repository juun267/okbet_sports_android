package org.cxct.sportlottery.network.outright.season


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Season(
    @Json(name = "end")
    val end: Long,
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "num")
    val num: Int,
    @Json(name = "start")
    val start: Long?
)