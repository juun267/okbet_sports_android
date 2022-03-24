package org.cxct.sportlottery.network.bet.settledDetailList

data class MatchOddsVO(
    val awayId: String?,
    val awayName: String?,
    val endTime: String?,
    val extInfo: String?,
    val hkOdds: Double?,
    val homeId: String?,
    val homeName: String?,
    val indoOdds: Double?,
    val leagueName: String?,
    val malayOdds: Double?,
    val matchId: String?,
    val odds: Double?,
    val oddsId: String?,
    val oddsType: String?,
    val playCateCode: String?,
    val playCateId: Int?,
    val playCateMatchResult: String?,
    val playCateName: String?,
    val playCode: String?,
    val playId: Int?,
    val playName: String?,
    val rtScore: String?,
    val spread: String?,
    val startTime: Long?,
    val startTimeDesc: String?,
    val status: Int?
)

