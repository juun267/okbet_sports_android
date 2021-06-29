package org.cxct.sportlottery.network.sport

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SportMenuFavoriteResult(
    @Json(name = "msg")
    val msg: String = "",
    @Json(name = "code")
    val code: Int = 0,
    @Json(name = "t")
    val t: MyFavorite,
    @Json(name = "success")
    val success: Boolean = false
)