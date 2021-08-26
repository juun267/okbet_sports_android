package org.cxct.sportlottery.network.common

interface MatchInfo {
    val id: String
    var awayScore: Int?
    var homeScore: Int?
    var statusName: String?
    val leagueTime: Int?
}