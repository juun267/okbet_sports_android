package org.cxct.sportlottery.network.outright.add


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StatusNameI18n(
    @Json(name = "mapKey")
    val mapKey: String
)