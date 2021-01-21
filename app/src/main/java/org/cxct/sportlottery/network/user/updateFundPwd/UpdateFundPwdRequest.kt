package org.cxct.sportlottery.network.user.updateFundPwd

class UpdateFundPwdRequest(
    val userId: Long,
    val platformId: Long,
    val oldPassword: String,
    val newPassword: String
)