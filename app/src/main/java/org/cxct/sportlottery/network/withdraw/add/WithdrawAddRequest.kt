package org.cxct.sportlottery.network.withdraw.add

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class WithdrawAddRequest(
    val applyMoney: Double,
    val withdrawPwd: String,
    val id: Long,
    val bettingStationId: Int?,
    val appointmentDate: String?,
    val appointmentHour: String?,
    var appsFlyerId: String? = null,
    var appsFlyerKey: String? = null,
    var appsFlyerPkgName: String? = null,
    val clientType: Int = 2,
    val channelMode: Int? = null,
)