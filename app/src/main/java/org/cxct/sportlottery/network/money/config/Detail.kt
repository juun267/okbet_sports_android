package org.cxct.sportlottery.network.money.config

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Detail(
    @Json(name = "contract")
    val contract: String?,
    @Json(name = "countLimit")
    val countLimit: Long?,
    @Json(name = "currency")
    val currency: String?,
    @Json(name = "exchangeRate")
    val exchangeRate: Double?,
    @Json(name = "feeRate")
    val feeRate: Double?,
    @Json(name = "feeVal")
    val feeVal: Double?,
    @Json(name = "maxWithdrawMoney")
    val maxWithdrawMoney: Double?,
    @Json(name = "minWithdrawMoney")
    val minWithdrawMoney: Double?
)
