package org.cxct.sportlottery.ui.game.home.recommend

import org.cxct.sportlottery.network.odds.Odd

class OddBean(
    val playTypeCode: String,
    val oddList: MutableList<Odd>,
)