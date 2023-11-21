package org.cxct.sportlottery.network.common

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.repository.sConfigData

@KeepMembers
interface CaptchaRequest {
    var validCodeIdentity: String?
    var validCode: String?
    var ticket: String?//腾讯云滑块验证码参数
    var randstr: String?
    fun buildParams(params1: String,params2: String){
        if (sConfigData?.captchaType ==1){
            ticket = params1
            randstr = params2
            validCodeIdentity = null
            validCode = null
        }else{
            ticket = null
            randstr = null
            validCodeIdentity = params1
            validCode = params2
        }
    }
}