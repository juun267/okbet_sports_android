package org.cxct.sportlottery.network.index.login

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class ValidateLoginDeviceSmsRequest(
    val loginEnvInfo: String? = null,
    val validCode: String? = null,
    val loginSrc: Long
)
