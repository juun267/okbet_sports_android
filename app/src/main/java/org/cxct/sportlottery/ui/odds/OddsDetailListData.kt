package org.cxct.sportlottery.ui.odds

import org.cxct.sportlottery.network.odds.Odd
import java.util.*

data class OddsDetailListData(
    var gameType: String,
    var typeCodes: MutableList<String>,
    var name: String,
    var oddArrayList: List<Odd>,
    var isExpand: Boolean
)