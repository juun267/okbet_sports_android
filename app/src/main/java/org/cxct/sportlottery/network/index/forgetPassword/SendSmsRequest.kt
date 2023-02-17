package org.cxct.sportlottery.network.index.forgetPassword

data class SendSmsRequest(
    val phone: String ,//手机号码
    val validCodeIdentity: String,
    val validCode: String
//    val userName: String //账号 不用前端传了(2023/02/13)
)
