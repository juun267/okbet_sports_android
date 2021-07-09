package org.cxct.sportlottery.ui.game

import androidx.annotation.StringRes
import org.cxct.sportlottery.R

enum class BetRecordType(val code: List<Int>, @StringRes val typeName: Int) {
    ALL(listOf(0, 1, 2, 3, 4, 5, 6, 7), R.string.all_order),
    SETTLEMENT(listOf(2,3,4,5,6,7), R.string.settled_order),
    UNSETTLEMENT(listOf(0,1), R.string.not_settled_order),
}
