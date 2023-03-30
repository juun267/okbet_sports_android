package org.cxct.sportlottery.network.vip.thirdRebates


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class ThirdDebateBean(
    @Json(name = "debateList")
    val debateList: MutableList<Debate>,
    @Json(name = "userLevelId")
    val userLevelId: Int,
    @Json(name = "userLevelName")
    val userLevelName: String,
)