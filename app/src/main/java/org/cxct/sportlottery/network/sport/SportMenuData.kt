package org.cxct.sportlottery.network.sport


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class SportMenuData(
    @Json(name = "menu")
    val menu: Menu,
    @Json(name = "atStart")
    val atStart: Sport
)