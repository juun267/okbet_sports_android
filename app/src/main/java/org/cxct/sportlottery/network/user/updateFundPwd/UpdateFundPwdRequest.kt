package org.cxct.sportlottery.network.user.updateFundPwd

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
class UpdateFundPwdRequest(
    val userId: Long,
    val platformId: Long,
    val oldPassword: String,
    val newPassword: String
)