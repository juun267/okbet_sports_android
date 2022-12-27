package org.cxct.sportlottery.util

import org.cxct.sportlottery.R

object OddsModeUtil {
    /**
     * 接受任何赔率变化
     */
    private const val accept_any_odds = 0

    /**
     * 接受更好赔率变化
     */
    private const val accept_better_odds = 2

    /**
     * 永不接受赔率变化
     */
    private const val never_accept_odds_change = 1




    val currentSelectModeIndex: (Int) -> Int = {
        when (it) {
            0 -> accept_any_odds
            1 -> never_accept_odds_change
            2 -> accept_better_odds
            else -> accept_any_odds  //接受任何赔率变化
        }
    }


    val currentSelectModeText: (Int) -> String = {
        when (it) {
            0 -> LocalUtils.getString(R.string.accept_any_change_in_odds)
            1 -> LocalUtils.getString(R.string.accept_never_change_in_odds)
            2 -> LocalUtils.getString(R.string.accept_better_change_in_odds)
            else -> LocalUtils.getString(R.string.accept_any_change_in_odds)
        }
    }

    val currentSelectModeIndexWithText: (String) -> Int = {
        when (it) {
            LocalUtils.getString(R.string.accept_any_change_in_odds) -> accept_any_odds
            LocalUtils.getString(R.string.accept_better_change_in_odds) -> accept_better_odds
            LocalUtils.getString(R.string.accept_never_change_in_odds) -> never_accept_odds_change
            else -> accept_any_odds
        }
    }
}