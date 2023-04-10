package org.cxct.sportlottery.ui.sport.detail


import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.sport.detail.adapter.OddsDetailListData


class OnOddClickListener(val _getBetInfoList: (odd: Odd, oddsDetail: OddsDetailListData?, scoPlayCateNameForBetInfo: String?) -> Unit) {

    var clickOdd: Odd? = null

    fun getBetInfoList(odd: Odd, oddsDetail: OddsDetailListData?, scoPlayCateNameForBetInfo: String? = null) {
        clickOdd = odd
        _getBetInfoList(odd, oddsDetail, scoPlayCateNameForBetInfo)
    }

}