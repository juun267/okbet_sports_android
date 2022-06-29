package org.cxct.sportlottery.network.bet.add

import org.cxct.sportlottery.network.common.IdParams
import org.cxct.sportlottery.network.bet.Odd

data class BetAddRequest(
    val oddsList: List<Odd>,
    val stakeList: List<Stake>,
    val oddsChangeOption: Int,
    val loginSrc: Long,
    val deviceId: String,
    override val userId: Int? = null,
    override val platformId: Int? = null,
    val channelType: Int
) : IdParams
