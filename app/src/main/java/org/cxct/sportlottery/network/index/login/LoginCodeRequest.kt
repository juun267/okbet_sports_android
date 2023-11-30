package org.cxct.sportlottery.network.index.login

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.CaptchaRequest


@KeepMembers
data class LoginCodeRequest(
    var phoneNumberOrEmail: String,
): CaptchaRequest {
    override var validCodeIdentity: String?=null
    override var validCode: String?=null
    override var ticket: String?=null
    override var randstr: String?=null
}
