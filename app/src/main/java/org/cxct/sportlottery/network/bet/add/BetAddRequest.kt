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
    val channelType: Int,

//    否
//    投注类型
//    一般單注與串關傳0, 單注多選時傳1(篮球末位比分专用)，null当0处理
//    当前 betType==1时 oddsList输入每个玩法赔率，第一个OddsSO需要填写stake
    val betType: Int? = 1,
) : IdParams
