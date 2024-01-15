package org.cxct.sportlottery.network.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
@JsonClass(generateAdapter = true)
data class UserGameTypeDiscount(
    @Json(name = "discount")
    var discount: String,
    @Json(name = "gameType")
    val gameType: String
)
