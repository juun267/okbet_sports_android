package org.cxct.sportlottery.network.withdraw.add

data class WithdrawAddRequest(
    val applyMoney: Double,
    val withdrawPwd: String,
    val id: Long,
)