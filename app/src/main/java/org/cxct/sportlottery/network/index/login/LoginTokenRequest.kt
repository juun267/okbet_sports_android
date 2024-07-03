package org.cxct.sportlottery.network.index.login

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.LOGIN_SRC

@KeepMembers
data class LoginTokenRequest(
    val token: String,
    val loginSrc: Long = LOGIN_SRC,
    var inviteCode: String? = null,
    val deviceSn: String = Constants.deviceSn, //设备号（手机app登录必传）
)
