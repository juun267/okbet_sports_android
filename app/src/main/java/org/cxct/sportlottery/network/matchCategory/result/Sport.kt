package org.cxct.sportlottery.network.matchCategory.result


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Sport(
    @Json(name = "code")
    val code: String?,
    @Json(name = "name")
    val name: String?
)