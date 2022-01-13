package org.cxct.sportlottery.network.common

interface MatchInfo {
    val id: String

    val gameType: String?

    var awayScore: Int?
    var homeScore: Int?
    var statusName18n: String?
    var leagueTime: Int?
    var socketMatchStatus: Int? //赛事阶段状态id

    var homeTotalScore: Int?
    var awayTotalScore: Int?
    var homePoints: Int?
    var awayPoints: Int?
    var playCateNum: Int?
    var stopped:Int?

    //FT
    var homeCards: Int?
    var awayCards: Int?
}