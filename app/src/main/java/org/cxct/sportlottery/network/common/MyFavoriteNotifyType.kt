package org.cxct.sportlottery.network.common

enum class MyFavoriteNotifyType(val code: Int) {
    LEAGUE_ADD(0),
    LEAGUE_REMOVE(1),
    MATCH_ADD(2),
    MATCH_REMOVE(3),
    DETAIL_ADD(4),
    DETAIL_REMOVE(5)
}