package org.cxct.sportlottery.network.withdraw.add

data class WithdrawAddRequest(
    val applyMoney: Long,
    val withdrawPwd: String,
    val id: Long,
)