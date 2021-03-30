package org.cxct.sportlottery.ui.odds

import org.cxct.sportlottery.network.odds.detail.Odd

data class OddsDetailListData(
    var gameType: String, //GameType.HDP ...
    var typeCodes: MutableList<String>, //POPULAR,ALL,HDP&OU,GOAL,QATest
    var name: String, //大/小
    var oddArrayList: List<Odd>, //odds[]
) {
    var isExpand: Boolean = true
    var isMoreExpand: Boolean = false
}