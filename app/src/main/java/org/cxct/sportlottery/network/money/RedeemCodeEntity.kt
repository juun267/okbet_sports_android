package org.cxct.sportlottery.network.money

import com.squareup.moshi.Json

data class RedeemCodeEntity(
    @Json(name = "rewards")
    val rewards: Int?,//抢包金额
)
