package org.cxct.sportlottery.network.sport

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Sport(
    @Json(name = "num")
    val num: Int,
    @Json(name = "items")
    val items: List<Item>
) {
    var isSelect = false
}