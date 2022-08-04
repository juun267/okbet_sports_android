package org.cxct.sportlottery.network.index.chechBetting

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.bet.settledList.Row
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.index.config.ConfigData

@JsonClass(generateAdapter = true)
class CheckBettingResult (
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "t")
    val checkBettingData: CheckBettingData?
) : BaseResult()