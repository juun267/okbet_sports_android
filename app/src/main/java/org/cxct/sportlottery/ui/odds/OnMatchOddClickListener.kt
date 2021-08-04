package org.cxct.sportlottery.ui.odds

import org.cxct.sportlottery.network.odds.Odd

interface OnMatchOddClickListener {
    fun getBetInfoList(odd: Odd)
    fun removeBetInfoItem(odd: Odd)
}