package org.cxct.sportlottery.ui.game.data

import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType


data class SpecialEntrance(
    val entranceMatchType: MatchType,
    val gameType: GameType? = null,
    val couponCode:String? = null,
    val couponName:String? = null,
    val matchID:String? = null,
    val gameMatchType: MatchType? = null
)
