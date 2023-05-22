package org.cxct.sportlottery.network.index.sendSms

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class SmsRequest(
    val phone: String //手机号码，多个用豆号分割最多100个号码
)
