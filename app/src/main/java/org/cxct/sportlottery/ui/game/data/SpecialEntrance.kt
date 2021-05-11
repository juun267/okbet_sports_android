package org.cxct.sportlottery.ui.game.data

import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.SportType


enum class SpecialEntranceSource { HOME, LEFT_MENU, SHOPPING_CART }

data class SpecialEntrance(
    val matchType: MatchType,
    val sportType: SportType? = null,
)
