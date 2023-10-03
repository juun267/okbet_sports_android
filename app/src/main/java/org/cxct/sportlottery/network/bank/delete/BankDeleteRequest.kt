package org.cxct.sportlottery.network.bank.delete

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class BankDeleteRequest(
    val fundPwd: String,
    val id: String,
    val securityCode: String
)
