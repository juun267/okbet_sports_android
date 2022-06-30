package org.cxct.sportlottery.network.money

import com.squareup.moshi.Json
import org.cxct.sportlottery.network.common.BaseResult

class RedEnvelopePrizeResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "t")
    val redEnvelopePrize: RedEnvelopePrize?
) : BaseResult()

