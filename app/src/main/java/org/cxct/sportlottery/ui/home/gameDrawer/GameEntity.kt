package org.cxct.sportlottery.ui.home.gameDrawer

import org.cxct.sportlottery.network.match.Match
import org.cxct.sportlottery.ui.home.HomeGameDrawer

class GameEntity(
    val itemType: ItemType,
    val gameName: String?,
    val match: Match? = null,
    val isShowBottomLine: Boolean = false
)