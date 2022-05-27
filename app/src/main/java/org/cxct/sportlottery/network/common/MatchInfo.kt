package org.cxct.sportlottery.network.common

interface MatchInfo {
    val id: String

    val gameType: String?

    var awayScore: String?
    var homeScore: String?
    var statusName18n: String?
    var leagueTime: Int?
    var socketMatchStatus: Int? //赛事阶段状态id

    var homeTotalScore: Int?
    var awayTotalScore: Int?
    var homePoints: Int?
    var awayPoints: Int?
    var playCateNum: Int?
    //FT
    var homeCards: Int?
    var awayCards: Int?
    var homeCornerKicks: Int?
    var awayCornerKicks: Int?

    var stopped: Int? //是否计时停止 1:是 ，0：否
}