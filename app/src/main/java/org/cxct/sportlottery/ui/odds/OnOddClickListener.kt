package org.cxct.sportlottery.ui.odds

import org.cxct.sportlottery.network.odds.detail.Odd

interface OnOddClickListener {
    fun onAddToBetInfoList(odd: Odd)
}