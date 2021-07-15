package org.cxct.sportlottery.network.matchCategory.result

import com.squareup.moshi.Json

data class QuickPlayCate(
    @Json(name = "gameType")
    val gameType: String?,
    @Json(name = "code")
    val code: String?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "sort")
    val sort: Int,
)

