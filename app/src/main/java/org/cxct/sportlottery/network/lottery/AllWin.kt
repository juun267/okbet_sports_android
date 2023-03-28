package org.cxct.sportlottery.network.lottery

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguard.KeepMembers

@JsonClass(generateAdapter = true)
@KeepMembers
data class AllWin(
    @Json(name = "prizeName")
    val prizeName: String,
    @Json(name = "prizeValue")
    val prizeValue: Int,
    @Json(name = "userName")
    val userName: String,
)