package org.cxct.sportlottery.network.user.updatePwd

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
class UpdatePwdRequest(
    val userId: Long,
    val platformId: Long,
    val oldPassword: String,
    val newPassword: String,
)