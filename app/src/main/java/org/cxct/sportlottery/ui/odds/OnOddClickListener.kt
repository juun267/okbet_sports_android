package org.cxct.sportlottery.ui.odds

import org.cxct.sportlottery.network.odds.detail.Odd

interface OnOddClickListener {
    fun getBetInfoList(odd: Odd, oddsDetail: OddsDetailListData)
    fun removeBetInfoItem(odd: Odd)
}