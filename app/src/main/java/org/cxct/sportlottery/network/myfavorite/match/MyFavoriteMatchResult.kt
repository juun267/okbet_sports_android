package org.cxct.sportlottery.network.myfavorite.match

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.odds.list.LeagueOdd

@JsonClass(generateAdapter = true) @KeepMembers
data class MyFavoriteMatchResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "rows")
    val rows: List<LeagueOdd>?,
    @Json(name = "total")
    val total: Int?
) : BaseResult()

