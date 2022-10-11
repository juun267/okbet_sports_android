package org.cxct.sportlottery.network.index.forgetPassword

data class ResetPasswordRequest(
    val phone: String ,//手机号码
    val confirmPassword: String, //确认密码
    val newPassword: String, //新密码
//    val userId: Int, //用户id
//    val platformId: Int //平台id
)
