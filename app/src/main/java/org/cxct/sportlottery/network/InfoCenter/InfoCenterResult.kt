package org.cxct.sportlottery.network.InfoCenter

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true)
data class InfoCenterResult(
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "code")
    override val code: Int,
    @Json(name = "rows")
    val infoCenterData: InfoCenterData?
): BaseResult()
