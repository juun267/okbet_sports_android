package org.cxct.sportlottery.network.outright.odds


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Sport(
    @Json(name = "code")
    val code: String?,
    @Json(name = "name")
    val name: String?
)