package org.cxct.sportlottery.ui.finance.df

enum class CheckStatus(val code: Int) {
    PROCESSING(1),
    PROCESSING_TWO(2),
    PASS(3),
    UN_PASS(4),
    BetStation(7) // 比照pc當作1處理
}