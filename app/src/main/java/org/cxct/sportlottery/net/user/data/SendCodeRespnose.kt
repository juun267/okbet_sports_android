package org.cxct.sportlottery.net.user.data

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
class SendCodeRespnose(
    val userName: String?, //用户名
    val msg: String?, //信息
    val vipType:String?,
    val firstPhoneGiveMoney:Boolean
)