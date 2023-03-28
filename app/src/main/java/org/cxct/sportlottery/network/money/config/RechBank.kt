package org.cxct.sportlottery.network.money.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class RechBank(
    @Json(name = "bankName")
    val bankName: String?,
    @Json(name = "ico")
    val ico: String?,
    @Json(name = "value")
    val value: String?
)
