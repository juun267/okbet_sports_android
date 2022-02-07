package org.cxct.sportlottery.network.withdraw.uwcheck

import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams

data class ValidateTwoFactorRequest(
    val securityCode: Int? = null
)
