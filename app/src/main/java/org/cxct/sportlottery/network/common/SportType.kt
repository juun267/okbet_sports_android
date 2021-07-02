package org.cxct.sportlottery.network.common

enum class SportType(val code: String) {
    FOOTBALL("FT"),
    BASKETBALL("BK"),
    BADMINTON("BM"),
    TENNIS("TN"),
    VOLLEYBALL("VB");


    companion object {
        fun getSportType(code: String): SportType? {
            return when (code) {
                FOOTBALL.code -> FOOTBALL
                BASKETBALL.code -> BASKETBALL
                BADMINTON.code -> BADMINTON
                TENNIS.code -> TENNIS
                VOLLEYBALL.code -> VOLLEYBALL
                else -> null
            }
        }
    }
}