package org.cxct.sportlottery.network.appUpdate

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CheckAppVersionResult(
    @Json(name = "version")
    val version: String?,
    @Json(name = "miniVersion")
    val miniVersion: String?,
    @Json(name = "check")
    val check: String?,
    @Json(name = "name")
    val name: String?,
)