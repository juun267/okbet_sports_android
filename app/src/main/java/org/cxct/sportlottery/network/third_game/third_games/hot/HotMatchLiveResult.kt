package org.cxct.sportlottery.network.third_game.third_games.hot

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.proguard.KeepMembers
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true)
@KeepMembers
data class HotMatchLiveResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "t")
    val MatchLiveList: List<HotMatchLiveData>?,
) : BaseResult()