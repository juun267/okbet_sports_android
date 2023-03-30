package org.cxct.sportlottery.network.bet.settledDetailList


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true) @KeepMembers
data class BetSettledDetailListResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "rows")
    val rows: List<Row>?,
    @Json(name = "total")
    val total: Int?,
    @Json(name = "other")
    val other: Other?,
) : BaseResult()

