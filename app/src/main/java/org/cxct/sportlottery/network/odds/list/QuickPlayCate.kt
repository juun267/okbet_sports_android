package org.cxct.sportlottery.network.odds.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers
import org.cxct.sportlottery.network.odds.Odd

@JsonClass(generateAdapter = true) @KeepMembers
data class QuickPlayCate(
    @Json(name = "code")
    val code: String?,
    @Json(name = "gameType")
    val gameType: String?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "sort")
    val sort: Int?,
    @Json(name = "nameMap")
    val nameMap: Map<String?, String?>? = null
) {
    var isSelected = false
    val quickOdds: MutableMap<String, MutableList<Odd?>?> = mutableMapOf()

    var positionButtonPage = 0
    var positionButtonPairTab = 0
}