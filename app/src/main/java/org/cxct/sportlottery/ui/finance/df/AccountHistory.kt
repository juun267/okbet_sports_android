package org.cxct.sportlottery.ui.finance.df

enum class AccountHistory(val tranTypeGroup: String) {
    BET("bet"),
    RECHARGE("recharge"),
    WITHDRAW("withdraw"),
    ACTIVITY("activity"),
    CREDIT("credit")
}