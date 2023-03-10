package org.cxct.sportlottery.network.bettingStation


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true) @KeepMembers
data class AreaAllResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "t")
    val areaAll: AreaAll,
    @Json(name = "success")
    override val success: Boolean
) : BaseResult()