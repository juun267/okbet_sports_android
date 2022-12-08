package org.cxct.sportlottery.network.withdraw.add

data class WithdrawAddRequest(
    val applyMoney: Double,
    val withdrawPwd: String,
    val id: Long,
    val bettingStationId: Int?,
    val appointmentDate: String?,
    val appointmentHour: String?,
    var appsFlyerId: String? = null,
    val clientType: Int = 2,
)