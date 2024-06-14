package org.cxct.sportlottery.network.error

enum class HttpError(val code: Int) {
    UNAUTHORIZED(401),
    GO_TO_SERVICE_PAGE(403),
    BET_INFO_CLOSE(5014),
    KICK_OUT_USER(1004),
    DO_NOT_HANDLE(4003), //API Error code 4003 不處理
    BALANCE_IS_LOW(2800), //余额不足
    MAINTENANCE(2611), //2611 跳維護頁
    LOGIN_IN_OTHER_PLACE(1005) //2611 跳維護頁
}
