package org.cxct.sportlottery.network

data class LoginRequest(
    val account: String,
    val password: String,
    val loginSrc: Int
)
