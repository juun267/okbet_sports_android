package org.cxct.sportlottery.ui.game.home.gameTable4

import org.cxct.sportlottery.network.odds.list.MatchOdd

class GameEntity(
    val code: String?, //球種 code
    val name: String?, //球種名稱
    val num: Int, //該球種的比賽場次數量
    val matchOdds: List<MatchOdd> = listOf(),
    val otherMatch: List<OtherMatch>? = listOf()
) {
    var vpTableAdapter: Vp2GameTable4Adapter? = null
}

class OtherMatch(
    val code: String?,
    val name: String?,
    val num: Int
)