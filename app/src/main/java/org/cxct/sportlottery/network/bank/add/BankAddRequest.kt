package org.cxct.sportlottery.network.bank.add

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class BankAddRequest(
    val bankName: String,
    val subAddress: String? = null,
    val cardNo: String,
    val fundPwd: String,
    val fullName: String? = null,
    val id: String? = null,
    val uwType: String,
    val userId: String? = null,
    val bankCode: String? = null
)