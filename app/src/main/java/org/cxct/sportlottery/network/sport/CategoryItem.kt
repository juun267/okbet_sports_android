package org.cxct.sportlottery.network.sport


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.odds.list.LeagueOdd

@JsonClass(generateAdapter = true)
@KeepMembers
open class CategoryItem(
    @Json(name = "id")
    val id: String,
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "icon")
    val icon: String,
    @Json(name = "num")
    var num: Int,
    @Json(name = "sort")
    val sort: Int
) {
    var isSelected: Boolean = false
    var categoryCodeList: List<String>? = null
}