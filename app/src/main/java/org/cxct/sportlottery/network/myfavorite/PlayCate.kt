package org.cxct.sportlottery.network.myfavorite

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class PlayCate(
    @Json(name = "gameType")
    val gameType: String?,
    @Json(name = "code")
    val code: String?,
)
