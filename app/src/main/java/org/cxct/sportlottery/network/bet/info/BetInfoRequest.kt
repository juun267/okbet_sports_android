package org.cxct.sportlottery.network.bet.info

import org.cxct.sportlottery.common.IdParams

data class BetInfoRequest(
    val oddsType: String,
    val oddsList: List<Odd>,
    val idParams: IdParams? = null
)
