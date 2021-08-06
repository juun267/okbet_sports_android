package org.cxct.sportlottery.ui.game.data

import org.cxct.sportlottery.network.common.TimeRangeParams

data class Date(
    val display: String,
    val timeRangeParams: TimeRangeParams,
    val date: String? = null,
    val isDateFormat: Boolean = false,
) {
    var isSelected: Boolean = false
}
