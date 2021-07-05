package org.cxct.sportlottery.network.sport.query


import com.squareup.moshi.Json

data class Play(
    @Json(name = "code")
    val code: String?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "num")
    val num: Int?
)