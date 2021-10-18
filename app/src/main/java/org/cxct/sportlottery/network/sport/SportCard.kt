package org.cxct.sportlottery.network.sport

import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType

data class SportCard(
    val gameType: GameType,
    val matchType: MatchType?
)
