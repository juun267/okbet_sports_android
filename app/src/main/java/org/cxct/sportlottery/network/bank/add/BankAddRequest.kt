package org.cxct.sportlottery.network.bank.add

data class BankAddRequest(
    val bankName: String,
    val subAddress: String,
    val cardNo: String,
    val fundPwd: String,
    val fullName: String,
    val id: String?,
    val uwType: String,
    val userId: String?
)