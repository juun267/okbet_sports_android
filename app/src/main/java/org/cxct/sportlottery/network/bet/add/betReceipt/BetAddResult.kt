package org.cxct.sportlottery.network.bet.add.betReceipt


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true)
@KeepMembers
data class BetAddResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override var msg: String,
    @Json(name = "success")
    override var success: Boolean,
    @Json(name = "t")
    val receipt: Receipt?
): BaseResult()