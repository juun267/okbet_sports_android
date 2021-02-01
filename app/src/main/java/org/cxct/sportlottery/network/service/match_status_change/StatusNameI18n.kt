package org.cxct.sportlottery.network.service.match_status_change


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StatusNameI18n(
    @Json(name = "en")
    val en: String? = "",
    @Json(name = "vi")
    val vi: String? = "",
    @Json(name = "zh")
    val zh: String? = "",
    @Json(name = "zh-TW")
    val zhTW: String? = ""
)