package org.cxct.sportlottery.network.user.setWithdrawInfo


import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class WithdrawInfoRequest(
    var email: String? = null,
    var fullName: String? = null,
    var fundPwd: String? = null,
    var phone: String? = null,
    var qq: String? = null,
    val userId: Long,
    var wechat: String? = null,
    val firstName: String? = null,
    val middelName: String? = null,
    val lastName: String? = null,
)