package org.cxct.sportlottery.network.bettingStation


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true) @KeepMembers
data class BettingStationResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "rows")
    val bettingStationList: List<BettingStation>,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "total")
    val total: Int
) : BaseResult()