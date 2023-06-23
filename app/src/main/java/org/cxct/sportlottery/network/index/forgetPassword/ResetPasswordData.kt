package org.cxct.sportlottery.network.index.forgetPassword

data class ResetPasswordData(
    val userName: String?, //用户名
    val msg: String?, //信息
    val vipType: Int?
)