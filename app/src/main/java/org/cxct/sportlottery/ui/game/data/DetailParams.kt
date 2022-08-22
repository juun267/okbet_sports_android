package org.cxct.sportlottery.ui.game.data

import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import java.io.Serializable

data class DetailParams(
    val matchType: MatchType?,
    val gameType: GameType,
    val matchId: String?
    ): Serializable