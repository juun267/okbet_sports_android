package org.cxct.sportlottery.network.third_game

import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.BaseResult

@JsonClass(generateAdapter = true)
data class BlankResult (
    override val code: Int,
    override val msg: String,
    override val success: Boolean
) : BaseResult()
