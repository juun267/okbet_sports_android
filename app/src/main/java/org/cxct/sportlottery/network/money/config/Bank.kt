package org.cxct.sportlottery.network.money.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Bank(
    @Json(name = "name")
    val name: String?,
    @Json(name = "value")
    val value: String?
)
