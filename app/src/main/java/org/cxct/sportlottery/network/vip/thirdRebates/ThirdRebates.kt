package org.cxct.sportlottery.network.vip.thirdRebates


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ThirdRebates(
    @Json(name = "firmCode")
    val firmCode: String,
    @Json(name = "firmType")
    val firmType: String,
    @Json(name = "GameFirms")
    val gameFirms: List<GameFirm>,
    @Json(name = "thirdDebateBeans")
    val thirdDebateBeans: List<ThirdDebateBean>
)