package org.cxct.sportlottery.ui.finance.data

data class WithdrawState(
    val code: Int?,
    val state: String,
) {
    var isSelected = false
}
