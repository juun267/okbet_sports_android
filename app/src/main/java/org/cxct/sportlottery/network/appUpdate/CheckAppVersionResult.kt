package org.cxct.sportlottery.network.appUpdate

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class CheckAppVersionResult(
    @Json(name = "version")
    val version: String?,
    @Json(name = "miniVersion")
    val miniVersion: String?,
    @Json(name = "check")
    val check: String?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "filename")
    val fileName: String?,
    @Json(name = "storeURL")
    val storeURL: String?,
    @Json(name = "storeURL1")
    val storeURL1: String?,
    @Json(name = "control_version")
    val controlVersion: String?,
)