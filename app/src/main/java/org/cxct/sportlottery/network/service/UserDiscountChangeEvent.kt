package org.cxct.sportlottery.network.service

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.service.ServiceEventType

@JsonClass(generateAdapter = true)
data class UserDiscountChangeEvent(
    @Json(name = "discount")
    val discount: Double?
)
