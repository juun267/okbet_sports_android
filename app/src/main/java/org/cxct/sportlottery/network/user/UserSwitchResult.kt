package org.cxct.sportlottery.network.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true)
data class UserSwitchResult(
    @Json(name = "code")
    override val code: Int,
//    @Json(name = "msg")
    override val msg: String="",
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "t")
    val t: Boolean
) : BaseResult() {
}