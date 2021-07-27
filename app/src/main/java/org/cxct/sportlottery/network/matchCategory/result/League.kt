package org.cxct.sportlottery.network.matchCategory.result


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class League(
    @Json(name = "category")
    val category: String?,
    @Json(name = "id")
    val id: String?,
    @Json(name = "name")
    val name: String?
)