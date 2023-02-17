package org.cxct.sportlottery.network.index.login

import org.cxct.sportlottery.repository.LOGIN_SRC


data class LoginTokenRequest(
    val token: String,
    val loginSrc: Long = LOGIN_SRC,
)