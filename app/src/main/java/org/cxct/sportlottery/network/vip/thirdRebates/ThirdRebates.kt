package org.cxct.sportlottery.network.vip.thirdRebates


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class ThirdRebates(
    @Json(name = "thirdDebateBeans")
    val thirdDebateBeans: List<ThirdDebateBean>
)