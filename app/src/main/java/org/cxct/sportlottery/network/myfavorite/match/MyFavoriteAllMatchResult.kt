package org.cxct.sportlottery.network.myfavorite.match

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true)
data class MyFavoriteAllMatchResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "rows")
    val rows: List<MyFavoriteAllMatchItem>?,
    @Json(name = "total")
    val total: Int?,
) : BaseResult()
