package org.cxct.sportlottery.ui.main.entity

class GameCateData(var category: GameCategory,
                   var tabDataList: MutableList<GameTabData> = mutableListOf(),
                   var isShowTabLayout: Boolean = true)