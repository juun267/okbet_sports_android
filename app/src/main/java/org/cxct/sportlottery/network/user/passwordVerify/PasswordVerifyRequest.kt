package org.cxct.sportlottery.network.user.passwordVerify

import org.cxct.sportlottery.common.proguards.KeepMembers

/**
 * @author kevin
 * @create 2022/6/6
 * @description
 */
@KeepMembers
data class PasswordVerifyRequest(
    val password: String
)
