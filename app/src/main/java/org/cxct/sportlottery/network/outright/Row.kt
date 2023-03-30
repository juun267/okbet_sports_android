package org.cxct.sportlottery.network.outright


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Row(
    @Json(name = "resultList")
    val resultList: List<Result>,
    @Json(name = "season")
    val season: Season
)