package org.cxct.sportlottery.network.bet.add

import org.cxct.sportlottery.network.common.IdParams
import org.cxct.sportlottery.network.bet.Odd

data class BetAddRequest(
    val oddsList: List<Odd>,
    val stakeList: List<Stake>,
    val oddsChangeOption: Int,
    val oddsType: String,
    val idParams: IdParams? = null
)
