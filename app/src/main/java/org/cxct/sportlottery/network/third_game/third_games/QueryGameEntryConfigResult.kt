package org.cxct.sportlottery.network.third_game.third_games


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.BaseResult
import org.cxct.sportlottery.network.sport.list.Row

@JsonClass(generateAdapter = true)
data class QueryGameEntryConfigResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "rows")
    val rows: List<QueryGameEntryData>?
): BaseResult()