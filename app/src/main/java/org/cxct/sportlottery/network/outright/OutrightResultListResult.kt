package org.cxct.sportlottery.network.outright


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true) @KeepMembers
data class OutrightResultListResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "rows")
    val rows: List<Row>?,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "total")
    val total: Int?
) : BaseResult()