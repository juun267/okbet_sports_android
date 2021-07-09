package org.cxct.sportlottery.ui.transactionStatus

import androidx.annotation.StringRes
import org.cxct.sportlottery.R

enum class GameType(val key: String, @StringRes val string: Int) {
    FT("FT", R.string.soccer),
    BK("BK", R.string.basketball),
    TN("TN", R.string.tennis),
    VB("VB", R.string.volleyball)
}