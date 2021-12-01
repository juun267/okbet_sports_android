package org.cxct.sportlottery.network.vip.thirdRebates


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ThirdDebateBean(
    @Json(name = "debateList")
    val debateList: MutableList<Debate>,
    @Json(name = "userLevelId")
    val userLevelId: Int,
    @Json(name = "userLevelName")
    val userLevelName: String,
)