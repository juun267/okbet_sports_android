package org.cxct.sportlottery.network.lottery


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.BaseResult


@JsonClass(generateAdapter = true)
data class LotteryResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "t")
    val t: LotteryInfo?,
    @Json(name = "success")
    override val success: Boolean,
) : BaseResult()