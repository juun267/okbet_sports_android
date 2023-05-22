package org.cxct.sportlottery.network.withdraw.uwcheck

import org.cxct.sportlottery.common.proguards.KeepMembers


@KeepMembers
data class ValidateTwoFactorRequest(
    val securityCode: String? = null
)
