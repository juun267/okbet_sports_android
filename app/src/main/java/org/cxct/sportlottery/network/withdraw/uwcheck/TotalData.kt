package org.cxct.sportlottery.network.withdraw.uwcheck

import com.squareup.moshi.Json
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
class TotalData(
    @Json(name = "deductMoney")
    val deductMoney: Double?,
    @Json(name = "unFinishValidAmount")
    val unFinishValidAmount: Double?
)