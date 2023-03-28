package org.cxct.sportlottery.network.matchresult.playlist


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true) @KeepMembers
data class MatchResultPlayListResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "rows")
    val matchResultPlayList: List<MatchResultPlayList>?,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "total")
    val total: Int
) : BaseResult()