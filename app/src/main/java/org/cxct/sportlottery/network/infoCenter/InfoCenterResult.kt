package org.cxct.sportlottery.network.infoCenter

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true) @KeepMembers
data class InfoCenterResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "rows")
    val infoCenterData: MutableList<InfoCenterData>?,
    @Json(name = "total")
    val total: Int?
) : BaseResult(){
    var page: Int? = 0
}
