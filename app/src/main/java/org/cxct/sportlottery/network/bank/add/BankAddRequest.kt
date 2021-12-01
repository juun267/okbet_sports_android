package org.cxct.sportlottery.network.bank.add

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