package org.cxct.sportlottery.network.outright.season


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Season(
    @Json(name = "id")
    val id: String?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "num")
    val num: Int?,
) {
    var isPin = false
}