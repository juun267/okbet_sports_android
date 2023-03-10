package org.cxct.sportlottery.net.user.data

import org.cxct.sportlottery.proguard.KeepMembers

@KeepMembers
class SendCodeRespnose(
    val userName: String?, //用户名
    val msg: String? //信息
)