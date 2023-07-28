package org.cxct.sportlottery.network.money

import com.squareup.moshi.Json

data class RedeemCodeHistoryEntity(
    @Json(name = "date")
    val date: String?,
    @Json(name = "redeemCode")
    val redeemCode: String?,
    @Json(name = "rewards")
    val rewards: String?,
)
