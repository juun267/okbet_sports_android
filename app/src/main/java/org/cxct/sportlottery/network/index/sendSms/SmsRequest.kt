package org.cxct.sportlottery.network.index.sendSms

data class SmsRequest(
    val platformId: Long, //平台id
    val phone: String //手机号码，多个用豆号分割最多100个号码
)
