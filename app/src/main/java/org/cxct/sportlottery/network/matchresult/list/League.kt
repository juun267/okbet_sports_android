package org.cxct.sportlottery.network.matchresult.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class League(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String
)