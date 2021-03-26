package org.cxct.sportlottery.ui.results

import androidx.annotation.StringRes
import org.cxct.sportlottery.R

data class SettlementFilter(
    var gameType: String = "FT",
    var gameZone: Set<Int>? = null,
    val filterKeyWord: String? = null
)

enum class GameType(val key: String, @StringRes val string: Int) {
    FT("FT", R.string.soccer),
    BK("BK", R.string.basketball),
    TN("TN", R.string.tennis),
    BM("BM", R.string.badminton),
    VB("VB", R.string.volleyball)
}