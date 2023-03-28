package org.cxct.sportlottery.network.odds.eps

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.odds.Odd

@JsonClass(generateAdapter = true) @KeepMembers
data class EpsOdd(
    @Json(name = "EPS")
    val eps: List<Odd>?
)