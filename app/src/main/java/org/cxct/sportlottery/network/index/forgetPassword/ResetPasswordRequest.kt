package org.cxct.sportlottery.network.index.forgetPassword

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class ResetPasswordRequest(
    val userName: String ,//手机号码
    val confirmPassword: String, //确认密码
    val newPassword: String, //新密码
    val phone: String? = null,
    val validCode: String? = null
)
