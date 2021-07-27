package org.cxct.sportlottery.util

import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.bet.list.INPLAY

object MatchOddUtil {
    fun transfer(
        matchType: MatchType,
        gameType: String,
        playCateName: String,
        playName: String?,
        matchInfo: MatchInfo,
        odd: Odd
    ): org.cxct.sportlottery.network.bet.info.MatchOdd? {
        matchInfo.id.let { matchId ->
            odd.id?.let { oddsId ->
                odd.odds?.let { odds ->
                    odd.hkOdds?.let { hkOdds ->
                        odd.producerId?.let { producerId ->
                            return org.cxct.sportlottery.network.bet.info.MatchOdd(
                                awayName = matchInfo.awayName,
                                homeName = matchInfo.homeName,
                                inplay = if (matchType == MatchType.IN_PLAY) INPLAY else 0,
                                leagueId = "",
                                leagueName = "",
                                matchId = matchId,
                                odds = odds,
                                hkOdds = hkOdds,
                                oddsId = oddsId,
                                playCateId = 0,
                                playCateName = playCateName,
                                playCode = "",
                                playId = 0,
                                playName = playName ?: "",
                                producerId = producerId,
                                spread = odd.spread ?: "",
                                startTime = matchInfo.startTime.toLong(),
                                status = odd.status,
                                gameType = gameType,
                                homeScore = matchInfo.homeScore ?: 0,
                                awayScore = matchInfo.awayScore ?: 0
                            )
                        }
                    }
                }
            }

        }
        return null
    }
}