package org.cxct.sportlottery.network.error

import org.cxct.sportlottery.common.proguards.KeepMembers


@KeepMembers
data class APIError(
    val code: Int? = null,
    val msg: String? = null,
    val success: Boolean? = null,
)
