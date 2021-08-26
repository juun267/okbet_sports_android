package org.cxct.sportlottery.network.common

interface MatchInfo {
    val id: String
    val awayScore: Int?
    val homeScore: Int?
    val statusName: String?
    val leagueTime: Int?
}