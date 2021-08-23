package org.cxct.sportlottery.ui.bet.list

import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.ui.base.ChannelType

class BetInfoListData(
    var matchOdd: MatchOdd,
    var parlayOdds: ParlayOdd?
) {
    var matchType: MatchType? = null
    var input: String? = null
    var oddsHasChanged = false
    var betAmount: Double = 0.0
    var amountError: Boolean = false
    var pointMarked: Boolean = false //紅色標記, 紀錄是否能串關
    var subscribeChannelType: ChannelType = ChannelType.EVENT //給投注單訂閱頻道使用
    var playCateMenuCode: String? = null //HallChannel訂閱需要
}

