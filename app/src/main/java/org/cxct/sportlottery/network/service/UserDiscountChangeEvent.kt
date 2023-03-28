package org.cxct.sportlottery.network.service

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class UserDiscountChangeEvent(
    @Json(name = "discount")
    val discount: Double?
)
