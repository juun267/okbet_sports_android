package org.cxct.sportlottery.network.odds.eps

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult


@JsonClass(generateAdapter = true) @KeepMembers
data class OddsEpsListResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "rows")
    val rows: List<OddsEpsListData>,
    @Json(name = "total")
     val total: Int
) : BaseResult()