package org.cxct.sportlottery.ui.finance

enum class RechargeState(val code: Double) {
    PROCESSING(1.toDouble()),
    SUCCESS(2.toDouble()),
    FAILED(3.toDouble()),
    RECHARGING(4.toDouble())
}