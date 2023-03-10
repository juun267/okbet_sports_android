package org.cxct.sportlottery.network.sport.query


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class PlayCate(
    @Json(name = "code")
    val code: String?,
    @Json(name = "name")
    val name: String?
) {
    var isSelected = false
}