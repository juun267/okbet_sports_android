package org.cxct.sportlottery.network.index.login

import org.cxct.sportlottery.common.proguards.KeepMembers


@KeepMembers
data class LoginCodeRequest(
    var phoneNumberOrEmail: String,
    var validCodeIdentity: String? = null,
    var validCode: String? = null,
)
