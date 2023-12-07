package org.cxct.sportlottery.ui.finance.df

import org.cxct.sportlottery.R

enum class AccountHistory(val tranTypeGroup: String) {
    ALL(""),
    BET("bet"),
    RECHARGE("recharge"),
    WITHDRAW("withdraw"),
    ACTIVITY("activity"),
    CREDIT("credit"),
    THIRD("third");
    companion object {
        fun getShowName(code: String?): Int {
            return when (code) {
                ALL.tranTypeGroup -> R.string.label_all
                BET.tranTypeGroup -> R.string.text_account_history_bet
                RECHARGE.tranTypeGroup -> R.string.text_account_history_recharge
                WITHDRAW.tranTypeGroup -> R.string.text_account_history_withdraw
                ACTIVITY.tranTypeGroup -> R.string.text_account_history_activity
                CREDIT.tranTypeGroup -> R.string.text_account_history_credit
                THIRD.tranTypeGroup -> R.string.third_party
                else -> R.string.text_account_history_bet
            }
        }
   }
}