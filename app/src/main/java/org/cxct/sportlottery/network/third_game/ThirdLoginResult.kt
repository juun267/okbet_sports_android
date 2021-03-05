package org.cxct.sportlottery.network.third_game

import org.cxct.sportlottery.network.common.BaseResult

class ThirdLoginResult(
    override val code: Int,
    override val msg: String,
    override val success: Boolean
) : BaseResult()