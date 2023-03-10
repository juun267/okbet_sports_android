package org.cxct.sportlottery.network.index.config


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Currency(
    @Json(name = "currency")
    val currency: String?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "sign")
    val sign: String?
) {
    var isSelected: Boolean = false
}