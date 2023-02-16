package org.cxct.sportlottery.network.index.login


data class LoginCodeRequest(
    var phoneNumberOrEmail: String,
    var validCodeIdentity: String? = null,
    var validCode: String? = null,
)
