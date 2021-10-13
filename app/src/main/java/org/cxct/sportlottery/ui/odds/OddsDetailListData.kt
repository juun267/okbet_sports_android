package org.cxct.sportlottery.ui.odds

import org.cxct.sportlottery.network.odds.Odd

data class OddsDetailListData(
    var gameType: String, //GameType.HDP ...
    var typeCodes: MutableList<String>, //POPULAR,ALL,HDP&OU,GOAL,QATest
    var name: String, //大/小
    var oddArrayList: List<Odd?>, //odds[]
    val nameMap: Map<String?, String?>?, //保存各语系name对应值的map
    val rowSort: Int //排序
) {
    var isExpand: Boolean = true
    var isMoreExpand: Boolean = false
    var gameTypeFgLgSelect = FGLGType.FG
    var groupItem = HashMap<String, List<Odd?>>()
    var gameTypeSCOSelect: String? = null
    var scoItem = HashMap<String, List<Odd?>>()
    var isPin = false
    var originPosition = 0
}

enum class FGLGType {
    FG, LG
}

