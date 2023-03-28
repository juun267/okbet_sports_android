package org.cxct.sportlottery.ui.maintab.entity

class GameCateData(var categoryThird: ThirdGameCategory,
                   var tabDataList: MutableList<GameTabData> = mutableListOf(),
                   var isShowTabLayout: Boolean = true)