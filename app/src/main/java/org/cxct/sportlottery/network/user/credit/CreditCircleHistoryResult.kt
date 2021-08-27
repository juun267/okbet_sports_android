package org.cxct.sportlottery.network.user.credit


import com.squareup.moshi.Json
import org.cxct.sportlottery.network.common.BaseResult

data class CreditCircleHistoryResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "rows")
    val rows: List<Row>?,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "total")
    val total: Int?,
    @Json(name = "other")
    val other: Row?
) : BaseResult()