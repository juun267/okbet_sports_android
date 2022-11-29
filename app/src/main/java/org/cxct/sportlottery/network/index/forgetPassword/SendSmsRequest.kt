package org.cxct.sportlottery.network.index.forgetPassword

data class SendSmsRequest(
    val phone: String ,//手机号码
    val userName: String //账号
)
