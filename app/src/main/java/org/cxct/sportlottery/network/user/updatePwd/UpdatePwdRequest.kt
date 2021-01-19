package org.cxct.sportlottery.network.user.updatePwd

class UpdatePwdRequest(
    val userId: Long,
    val oldPassword: String,
    val newPassword: String
)