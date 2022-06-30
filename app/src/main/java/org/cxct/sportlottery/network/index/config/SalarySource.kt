package org.cxct.sportlottery.network.index.config


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SalarySource(
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String
)