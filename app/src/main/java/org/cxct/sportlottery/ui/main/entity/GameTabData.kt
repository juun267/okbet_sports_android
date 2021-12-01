package org.cxct.sportlottery.ui.main.entity

import org.cxct.sportlottery.network.third_game.third_games.GameCategory
import org.cxct.sportlottery.network.third_game.third_games.GameFirmValues

class GameTabData(
    val gameCategory: GameCategory,
    val gameFirm: GameFirmValues?,
    var gameList: MutableList<GameItemData>
)