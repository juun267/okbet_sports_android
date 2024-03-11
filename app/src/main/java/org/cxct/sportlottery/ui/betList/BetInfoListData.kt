package org.cxct.sportlottery.ui.betList

import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.bet.settledDetailList.BetInfo
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.common.enums.ChannelType
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
class BetInfoListData(
    var matchOdd: MatchOdd,
    var parlayOdds: ParlayOdd?,
    var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?
): Cloneable {
    public override fun clone(): BetInfoListData {
        return super.clone() as BetInfoListData
    }
    var matchType: MatchType? = null
    var isInputBet = false //是否輸入本金
    var isInputWin = false //是否輸入可贏
    var input: String? = null
    var betAmount: Double = 0.0
    var inputBetAmountStr: String? = betAmount.toString()
        set(value) {
            field = if (value.isNullOrEmpty()) {
                betAmount.toString()
            } else {
                value
            }
        }

    var realAmount:Double = 0.0 //計算出來的實際下注金額
    var amountError: Boolean = false
    var pointMarked: Boolean = false //紅色標記, 紀錄是否能串關
    var subscribeChannelType: ChannelType = ChannelType.EVENT //給投注單訂閱頻道使用
    var playCateMenuCode: String? = null //HallChannel訂閱需要
    var outrightMatchInfo: MatchInfo? = null
    //新增此項是因應加入馬來盤&印尼盤後,有可能會下注時盤口會與使用者預選的盤口不同,所以每次都要記錄(預設是 EU)
    var singleBetOddsType: OddsType = OddsType.EU

    //保存betInfo
    var betInfo: BetInfo? = null
    //是否点击了,用于篮球末位比分购物车
    var isClickForBasketball:Boolean?  = false
}

