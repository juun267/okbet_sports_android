package org.cxct.sportlottery.network.index.login

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class CheckUserRequest(
    val phoneNumberOrEmail: String,
)
