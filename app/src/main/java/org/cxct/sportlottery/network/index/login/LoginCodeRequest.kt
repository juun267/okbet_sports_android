package org.cxct.sportlottery.network.index.login


data class LoginCodeRequest(
    var phoneNumberOrEmail: String,
    var validCode: String? = null,
)
