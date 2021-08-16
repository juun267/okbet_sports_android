package org.cxct.sportlottery.network.league


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Row(
    @Json(name = "list")
    val list: List<League>,
    @Json(name = "name")
    val name: String,
    @Json(name = "num")
    val num: Int,
    @Json(name= "icon")
    val icon: String
) {
    var isExpand = true
    var searchList = listOf<League>()
}