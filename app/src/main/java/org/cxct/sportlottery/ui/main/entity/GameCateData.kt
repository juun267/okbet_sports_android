package org.cxct.sportlottery.ui.main.entity

class GameCateData(var categoryThird: ThirdGameCategory,
                   var tabDataList: MutableList<GameTabData> = mutableListOf(),
                   var isShowTabLayout: Boolean = true)