package org.cxct.sportlottery.network.index.forgetPassword

data class ForgetPasswordSmsRequest(
    val phone: String ,//电话号码 (不含 国码)
    val validCode: String//验证码
)
