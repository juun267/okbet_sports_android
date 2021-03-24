package org.cxct.sportlottery.network.outright.season


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Row(
    @Json(name = "code")
    val code: String,
    @Json(name = "list")
    val list: List<Season>,
    @Json(name = "name")
    val name: String,
    @Json(name = "num")
    val num: Int
) {
    var isExpand = true
    var searchList = listOf<Season>()
}