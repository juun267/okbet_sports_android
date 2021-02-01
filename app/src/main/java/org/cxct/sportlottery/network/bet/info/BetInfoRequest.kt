package org.cxct.sportlottery.network.bet.info

import org.cxct.sportlottery.network.common.IdParams
import org.cxct.sportlottery.network.bet.Odd

data class BetInfoRequest(
    val oddsType: String,
    val oddsList: List<Odd>,
    override val userId: Int? = null,
    override val platformId: Int? = null
) : IdParams
