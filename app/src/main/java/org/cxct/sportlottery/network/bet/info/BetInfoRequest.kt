package org.cxct.sportlottery.network.bet.info

import org.cxct.sportlottery.network.common.IdParams
import org.cxct.sportlottery.network.bet.Odd

data class BetInfoRequest(
    val oddsType: String,
    val oddsList: List<Odd>,
    val idParams: IdParams? = null
)
