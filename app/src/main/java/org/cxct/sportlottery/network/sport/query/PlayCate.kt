package org.cxct.sportlottery.network.sport.query


import com.squareup.moshi.Json

data class PlayCate(
    @Json(name = "code")
    val code: String?,
    @Json(name = "name")
    val name: String?
) {
    var isSelected = false
}