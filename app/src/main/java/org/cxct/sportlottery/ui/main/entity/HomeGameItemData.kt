package org.cxct.sportlottery.ui.main.entity

import org.cxct.sportlottery.network.third_game.third_games.ThirdDictValues

class HomeGameItemData(var dataType: DataType? = null,
                       var thirdGameData: ThirdDictValues? = null, ) {
    enum class DataType { HEADER, THIRD_GAME }
}