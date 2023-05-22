package org.cxct.sportlottery.network.money

import com.squareup.moshi.Json
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class RedEnvelopePrize(
    @Json(name = "grabMoney")
    val grabMoney: String?,//抢包金额
    @Json(name = "betMoney")
    val betMoney: String?,//红包雨设置打码量
    @Json(name = "userBetMoney")
    val userBetMoney: String?,//用户实际打码量

)
