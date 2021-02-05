package org.cxct.sportlottery.network.feedback

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true)
data class FeedbackListResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
    @Json(name = "rows")
    val rows: MutableList<Rows>?,
    @Json(name = "total")
    val total: Int?
) : BaseResult()

class Rows(
    val addTime: String,
    val content: String,
    val feedbackCode: String,
    val id: Int,
    val lastFeedbackTime: String,
    val platformId: Int,
    val status: Int,
    val track: Int,
    val type: Int,
    val userId: Int,
    val userName: String
)