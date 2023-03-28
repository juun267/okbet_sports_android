package org.cxct.sportlottery.network.bet.settledList


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true) @KeepMembers
data class BetSettledListResult(
    override val code: Int,
    override val msg: String,
    override val success: Boolean,
    @Json(name = "other")
    val other: Other?,
    @Json(name = "rows")
    val rows: List<Row>?,
    @Json(name = "total")
    val total: Int?
): BaseResult()