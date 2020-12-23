package org.cxct.sportlottery.network.match


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.BaseResult


@JsonClass(generateAdapter = true)
data class MatchPreloadResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "t")
    val matchPreloadData: MatchPreloadData?
) : BaseResult() {

    //記錄：用來判斷是哪種 matchType 的請求，以利後續 UI 刷新
    var matchType: MatchType? = null
}