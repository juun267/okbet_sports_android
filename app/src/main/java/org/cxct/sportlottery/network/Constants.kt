package org.cxct.sportlottery.network

object Constants {
    const val BASE_URL = "https://sports.cxct.org"

    //bet
    const val MATCH_BET_INFO = "/api/front/match/bet/info"
    const val MATCH_BET_ADD = "/api/front/match/bet/add"
    const val MATCH_BET_LIST = "/api/front/match/bet/list"

    //index
    const val INDEX_LOGIN = "/api/front/index/login"
    const val INDEX_LOGOUT = "/api/front/index/logout"

    //league
    const val LEAGUE_LIST = "/api/front/match/league/list"

    //match
    const val MATCH_PRELOAD = "/api/front/match/preload"

    //match result
    const val MATCH_RESULT_LIST = "/api/front/match/result/list"
    const val MATCH_RESULT_PLAY_LIST = "/api/front/match/result/play/list"

    //message
    const val MESSAGE_LIST = "/api/front/message/list"

    //odds
    const val MATCH_ODDS_LIST = "/api/front/match/odds/list"
    const val MATCH_ODDS_DETAIL = "/api/front/match/odds/detail"

    //sport
    const val SPORT_MENU = "/api/front/sport/mobile/menu"

    //play category list
    const val PLAYCATE_TYPE_LIST = "/api/front/playcate/type/list"

    //outright
    const val OUTRIGHT_RESULT_LIST = "/api/front/outright/result/list"

    //user
    const val USER_MONEY = "/api/front/user/money"

    //timeout
    const val CONNECT_TIMEOUT: Long = 15 * 1000
    const val WRITE_TIMEOUT: Long = 15 * 1000
    const val READ_TIMEOUT: Long = 15 * 1000

}