package org.cxct.sportlottery.network.third_game.third_games

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class QueryGameEntryConfigRequest(
    val position: Int,
    val gameType: Int?,
)
