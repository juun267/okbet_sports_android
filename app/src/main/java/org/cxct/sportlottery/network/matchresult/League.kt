package org.cxct.sportlottery.network.matchresult


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class League(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String
)