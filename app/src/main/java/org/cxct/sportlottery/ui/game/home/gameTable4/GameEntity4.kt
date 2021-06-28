package org.cxct.sportlottery.ui.game.home.gameTable4

import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.match.Match
import org.cxct.sportlottery.network.service.match_clock.MatchClockCO
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusCO

class GameEntity4(
    val code: String?,
    val name: String?,
    val num: Int,
    val gameBeanList: List<GameBean>
)

class GameBean(
    val code: String?,
    val match: Match?,
    val matchType: MatchType?
) {
    var matchStatusCO: MatchStatusCO? = null
    var matchClockCO: MatchClockCO? = null
}