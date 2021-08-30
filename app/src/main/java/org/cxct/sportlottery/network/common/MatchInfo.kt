package org.cxct.sportlottery.network.common

interface MatchInfo {
    val id: String
    var awayScore: Int?
    var homeScore: Int?
    var statusName: String?
    var leagueTime: Int?

    var homeTotalScore: Int?
    var awayTotalScore: Int?
    var homePoints: Int?
    var awayPoints: Int?

    //FT
    var homeCornerKicks: Int?
    var awayCornerKicks: Int?
    var homeCards: Int?
    var awayCards: Int?
    var homeYellowCards: Int?
    var awayYellowCards: Int?
}