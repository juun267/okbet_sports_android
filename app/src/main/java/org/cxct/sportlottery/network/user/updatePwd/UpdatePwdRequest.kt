package org.cxct.sportlottery.network.user.updatePwd

class UpdatePwdRequest(
    val userId: Long,
    val platformId: Long,
    val oldPassword: String,
    val newPassword: String,
)