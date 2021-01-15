package org.cxct.sportlottery.network.index.validCode

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.index.validCode.ValidCodeData

@JsonClass(generateAdapter = true)
data class ValidCodeResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "t")
    val validCodeData: ValidCodeData?

) : BaseResult()