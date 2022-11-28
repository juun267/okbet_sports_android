package org.cxct.sportlottery.network.index.forgetPassword

data class ValidateUserRequest(
    val validCode: String ,//验证码
    val userName: String ,//用户账户
    val validCodeIdentity: String, //验证码标识
)
