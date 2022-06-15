package org.cxct.sportlottery.ui.odds

import org.cxct.sportlottery.network.odds.Odd

data class OddsDetailListData(
    var gameType: String, //GameType.HDP ...
    var typeCodes: MutableList<String>, //POPULAR,ALL,HDP&OU,GOAL,QATest
    var name: String, //大/小
    var oddArrayList: MutableList<Odd?>, //odds[]
    val nameMap: Map<String?, String?>?, //保存各语系name对应值的map
    var rowSort: Int //排序
) {
    var isExpand: Boolean = true
    var isMoreExpand: Boolean = false
    var gameTypeFgLgSelect = FGLGType.FG
    var isPin = false
    var originPosition = 0

    //SCO
    var gameTypeSCOSelect: String? = null
    var teamNameList = mutableListOf<String>()
    var scoItem = HashMap<String, List<Odd?>>() // 當前選中
    var homeMap = HashMap<String, List<Odd?>>() // 球員玩法主隊
    var awayMap = HashMap<String, List<Odd?>>() // 球員玩法客隊
}

enum class FGLGType {
    FG, LG
}

