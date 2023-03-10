package org.cxct.sportlottery.network.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true)
@KeepMembers
class BaseSecurityCodeResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean
) : BaseResult()