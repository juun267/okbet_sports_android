package org.cxct.sportlottery.network.match


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Data(
    @Json(name = "code")
    val code: String,
    @Json(name = "matchs")
    val matchs: List<Match>
)