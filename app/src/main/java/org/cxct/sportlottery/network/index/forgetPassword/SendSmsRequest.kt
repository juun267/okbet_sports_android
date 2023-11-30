package org.cxct.sportlottery.network.index.forgetPassword

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.CaptchaRequest

@KeepMembers
data class SendSmsRequest(
    val phone: String ,//手机号码
):CaptchaRequest{
    override var validCodeIdentity: String? = null
    override var validCode: String? = null
    override var ticket: String? = null//腾讯云滑块验证码参数
    override var randstr: String? = null
}
