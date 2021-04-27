package org.cxct.sportlottery.network.index.playquotacom

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.index.playquotacom.t.T

@JsonClass(generateAdapter = true)
data class PlayQuotaComResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "t")
    val t: T
) : BaseResult()