package org.cxct.sportlottery.network.withdraw.uwcheck

import com.squareup.moshi.Json

class TotalData(
    @Json(name = "deductMoney")
    val deductMoney: Double?,
    @Json(name = "unFinishValidAmount")
    val unFinishValidAmount: Double?
)