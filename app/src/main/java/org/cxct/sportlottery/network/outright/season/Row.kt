package org.cxct.sportlottery.network.outright.season


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Row(
    @Json(name = "list")
    val list: List<Season> = listOf(),
    @Json(name = "name")
    val name: String?,
    @Json(name = "sort")
    val sort: Int?,
    @Json(name = "num")
    val num: Int?
) {
    var isExpand = true
    var searchList = listOf<Season>()
}