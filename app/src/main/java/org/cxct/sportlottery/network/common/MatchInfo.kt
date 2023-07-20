package org.cxct.sportlottery.network.common

import org.cxct.sportlottery.network.service.match_status_change.MatchStatus


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
    var homePoints: String?
    var awayPoints: String?
    var playCateNum: Int?

    //FT
    var homeCards: Int
    var awayCards: Int
    var homeYellowCards: Int
    var awayYellowCards: Int
    var homeCornerKicks: Int
    var awayCornerKicks: Int
    var homeHalfScore: String?
    var awayHalfScore: String?

    //BB
    var attack: String?
    var halfStatus: Int?
    var firstBaseBag: Int?
    var secBaseBag: Int?
    var thirdBaseBag: Int?
    var outNumber: Int?

    //CK
    var homeOver: String?
    var awayOver: String?

    var stopped: Int? //是否计时停止 1:是 ，0：否
    var matchStatusList: List<MatchStatus>?
}