package org.cxct.sportlottery.ui.sport.common

import org.cxct.sportlottery.network.odds.Odd

class OddBean(
    val playTypeCode: String,
    val oddList: MutableList<Odd>,
)