package org.cxct.sportlottery.ui.game.home.gameDrawer

import org.cxct.sportlottery.network.match.Match

class GameEntity(
    val itemType: ItemType,
    val code: String?,
    val name: String?,
    val match: Match? = null,
    val isShowBottomLine: Boolean = false
)