package org.cxct.sportlottery.network.index.login

import cn.jpush.android.api.JPushInterface
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.repository.LOGIN_SRC

@KeepMembers
data class LoginTokenRequest(
    val token: String,
    val loginSrc: Long = LOGIN_SRC,
    var inviteCode: String? = null,
    val deviceSn: String = JPushInterface.getRegistrationID(MultiLanguagesApplication.getInstance()), //设备号（手机app登录必传）
)
