package org.cxct.sportlottery.network.match


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult


@JsonClass(generateAdapter = true) @KeepMembers
data class MatchPreloadResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "t")
    val matchPreloadData: MatchPreloadData?
) : BaseResult()
{
    var isSelected: Boolean = false
}