package org.cxct.sportlottery.network.index.chechBetting

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
class CheckBettingData(
    @Json(name = "id")
    val id: String? = null,
    @Json(name = "code")
    val code: String? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "nationCode")
    val nationCode: String? = null,
    @Json(name = "currency")
    val currency: String? = null
)