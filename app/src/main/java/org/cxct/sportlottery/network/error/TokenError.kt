package org.cxct.sportlottery.network.error

enum class TokenError(val code: Int) {
    EXPIRED(2014),
    FAILURE(2015),
    REPEAT_LOGIN(2018)
}