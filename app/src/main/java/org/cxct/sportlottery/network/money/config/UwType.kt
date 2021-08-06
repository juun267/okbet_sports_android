package org.cxct.sportlottery.network.money.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UwType(
    @Json(name = "detailList")
    val detailList: List<Detail>,
    @Json(name = "name")
    val name: String?,
    @Json(name = "open")
    val `open`: Int?,
    @Json(name = "sort")
    val sort: Int?,
    @Json(name = "type")
    val type: String?
)
