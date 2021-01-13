package org.cxct.sportlottery.ui.game.data

import org.cxct.sportlottery.network.common.TimeRangeParams

data class Date(
    val display: String,
    val timeRangeParams: TimeRangeParams,
) {
    var isSelected: Boolean = false
}
