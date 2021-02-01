package org.cxct.sportlottery.ui.finance.df

enum class Status(val code: Int) {
    PROCESSING(1),
    SUCCESS(2),
    FAILED(3),
    RECHARGING(4)
}