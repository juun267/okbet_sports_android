package org.cxct.sportlottery.network.matchCategory.result

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MatchCategoryData(
    @Json(name = "menu")
    val menu: List<Menu>?,
    @Json(name = "odds")
    val odds: List<OddData>?
)