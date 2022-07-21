package org.cxct.sportlottery.network.sport.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Row(
    @Json(name = "code")
    val code: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String,
    @Json(name = "sortNum")
    val sortNum: Int
)