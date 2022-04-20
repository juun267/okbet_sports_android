package org.cxct.sportlottery.network.error

enum class HttpError(val code: Int) {
    UNAUTHORIZED(401),
    BET_INFO_CLOSE(5014),
    KICK_OUT_USER(1004)
}
