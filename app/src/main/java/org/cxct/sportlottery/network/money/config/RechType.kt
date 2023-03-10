package org.cxct.sportlottery.network.money.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class RechType(
    @Json(name = "name")
    val name: String?,
    @Json(name = "value")
    val value: String?
)
