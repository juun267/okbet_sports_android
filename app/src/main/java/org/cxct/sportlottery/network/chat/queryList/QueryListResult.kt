package org.cxct.sportlottery.network.chat.queryList


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.net.chat.data.Row
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true)
data class QueryListResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "rows")
    val rows: List<Row>?,
    @Json(name = "total")
    val total: Int,
) : BaseResult()