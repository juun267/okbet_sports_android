package org.cxct.sportlottery.network.outright.season


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Row(
    @Json(name = "list")
    val list: List<Season> = listOf(),
    @Json(name = "name")
    val name: String?,
    @Json(name = "sort")
    val sort: Int?,
    @Json(name = "num")
    val num: Int?,
    @Json(name= "icon")
    val icon: String
) {
    var isExpand = true
    var searchList = listOf<Season>()
}