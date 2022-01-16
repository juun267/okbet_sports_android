package org.cxct.sportlottery.ui.odds


import org.cxct.sportlottery.network.odds.Odd


class OnOddClickListener(val _getBetInfoList: (odd: Odd, oddsDetail: OddsDetailListData?) -> Unit) {

    var clickOdd: Odd? = null

    fun getBetInfoList(odd: Odd, oddsDetail: OddsDetailListData?) {
        clickOdd = odd
        _getBetInfoList(odd, oddsDetail)
    }

}