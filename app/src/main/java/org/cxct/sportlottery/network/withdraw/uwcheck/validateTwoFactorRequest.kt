package org.cxct.sportlottery.network.withdraw.uwcheck


data class ValidateTwoFactorRequest(
    val securityCode: String? = null
)
