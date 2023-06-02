package org.cxct.sportlottery.network.chat.removeMessage


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.BaseResult

/**
 * @author kevin
 * @create 2023/3/9
 * @description
 */
@JsonClass(generateAdapter = true)
data class RemoveMessageResult(
    @Json(name = "code")
    override val code: Int,
    @Json(name = "msg")
    override val msg: String,
    @Json(name = "success")
    override val success: Boolean,
) : BaseResult()
