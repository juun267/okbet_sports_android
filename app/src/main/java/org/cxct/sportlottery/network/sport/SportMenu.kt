package org.cxct.sportlottery.network.sport

import androidx.annotation.DrawableRes
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType

data class SportMenu(
    val gameType: GameType,
    val sportName: String,
    val sportEnName: String?,
    @DrawableRes val icon: Int?
){
    var gameCount: Int = 0
    var entranceType: MatchType? = null
}
