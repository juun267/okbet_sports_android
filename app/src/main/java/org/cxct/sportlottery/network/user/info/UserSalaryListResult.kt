package org.cxct.sportlottery.network.user.info

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.index.config.SalarySource

@JsonClass(generateAdapter = true) @KeepMembers
class UserSalaryListResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "rows")
    val rows: List<SalarySource>?
):BaseResult() {
}