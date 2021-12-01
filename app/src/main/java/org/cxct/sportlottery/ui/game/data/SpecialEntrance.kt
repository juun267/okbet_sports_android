package org.cxct.sportlottery.ui.game.data

import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType


data class SpecialEntrance(
    val matchType: MatchType,
    val gameType: GameType? = null,
)
