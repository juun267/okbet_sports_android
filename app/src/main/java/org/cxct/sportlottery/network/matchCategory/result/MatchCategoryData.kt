package org.cxct.sportlottery.network.matchCategory.result

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class MatchCategoryData(
    @Json(name = "menu")
    val menu: List<Menu>?,
    @Json(name = "odds")
    val odds: List<OddData>?,
    @Json(name = "playCateNameMap")
    var playCateNameMap: MutableMap<String?, Map<String?, String?>?>?
)