package org.cxct.sportlottery.network.today


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Row(
        @Json(name = "categoryDesc")
    val categoryDesc: String?,
        @Json(name = "categoryId")
    val categoryId: String?,
        @Json(name = "gameType")
    val gameType: String?,
        @Json(name = "id")
    val id: Int?,
        @Json(name = "matchList")
    val matchList: List<String> = listOf(),
        @Json(name = "matchNums")
    val matchNums: Int?,
        @Json(name = "sort")
    val sort: Int?
)