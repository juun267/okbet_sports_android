package org.cxct.sportlottery.ui.game.home.gameTable

import org.cxct.sportlottery.network.match.Match
import org.cxct.sportlottery.network.service.match_clock.MatchClockCO
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusCO

class GameEntity(
    val itemType: ItemType,
    val code: String?,
    val name: String?,
    val num: Int,
    val match: Match? = null
) {
    var matchStatusCO: MatchStatusCO? = null
    var matchClockCO: MatchClockCO? = null
}