package org.cxct.sportlottery.network.index.forgetPassword

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class ValidData(
    val userName: String?, //用户名
    val countDownSec: Int? //倒计时（秒数）
)