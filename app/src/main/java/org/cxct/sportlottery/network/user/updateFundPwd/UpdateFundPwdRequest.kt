package org.cxct.sportlottery.network.user.updateFundPwd

class UpdateFundPwdRequest(
    val userId: Long,
    val oldPassword: String,
    val newPassword: String
)