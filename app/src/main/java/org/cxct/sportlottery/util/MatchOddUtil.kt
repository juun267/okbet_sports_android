package org.cxct.sportlottery.util

import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.bet.list.INPLAY

object MatchOddUtil {

    fun transfer(
        matchType: MatchType,
        gameType: String,
        playCateName: String,
        playName: String,
        matchOdd: MatchOdd, odd: Odd
    ): org.cxct.sportlottery.network.bet.info.MatchOdd? {

        matchOdd.matchInfo?.id?.let { matchId ->
            odd.id?.let { oddsId ->
                odd.odds?.let { odds ->
                    odd.hkOdds?.let { hkOdds ->
                        odd.producerId?.let { producerId ->

                            return org.cxct.sportlottery.network.bet.info.MatchOdd(
                                awayName = matchOdd.matchInfo.awayName,
                                homeName = matchOdd.matchInfo.homeName,
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
                                playName = playName,
                                producerId = producerId,
                                spread = odd.spread ?: "",
                                startTime = matchOdd.matchInfo.startTime.toLong(),
                                status = odd.status,
                                gameType = gameType,
                                homeScore = matchOdd.matchInfo.homeScore ?: 0,
                                awayScore = matchOdd.matchInfo.awayScore ?: 0
                            )
                        }
                    }
                }
            }
        }

        return null
    }

    fun transfer(
        gameType: String,
        playCateName: String?,
        playName: String?,
        matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd,
        odd: Odd
    ): org.cxct.sportlottery.network.bet.info.MatchOdd? {

        odd.id?.let { oddsId ->
            odd.odds?.let { odds ->
                odd.hkOdds?.let { hkOdds ->
                    odd.producerId?.let { producerId ->

                        return org.cxct.sportlottery.network.bet.info.MatchOdd(
                            awayName = matchOdd.matchInfo.awayName,
                            homeName = matchOdd.matchInfo.homeName,
                            inplay = 0,
                            leagueId = "",
                            leagueName = "",
                            matchId = matchOdd.matchInfo.id,
                            odds = odds,
                            hkOdds = hkOdds,
                            oddsId = oddsId,
                            playCateId = 0,
                            playCateName = playCateName ?: "",
                            playCode = "",
                            playId = 0,
                            playName = playName ?: "",
                            producerId = producerId,
                            spread = odd.spread ?: "",
                            startTime = matchOdd.matchInfo.startTime.toLong(),
                            status = odd.status,
                            gameType = gameType,
                            homeScore = 0,
                            awayScore = 0
                        )
                    }
                }
            }
        }

        return null
    }

    fun transfer(
        matchType: MatchType,
        gameType: String,
        playCateName: String,
        matchOdd: org.cxct.sportlottery.network.odds.detail.MatchOdd,
        odd: Odd
    ): org.cxct.sportlottery.network.bet.info.MatchOdd? {

        odd.id?.let { oddsId ->
            odd.odds?.let { odds ->
                odd.hkOdds?.let { hkOdds ->
                    odd.producerId?.let { producerId ->

                        return org.cxct.sportlottery.network.bet.info.MatchOdd(
                            awayName = matchOdd.matchInfo.awayName,
                            homeName = matchOdd.matchInfo.homeName,
                            inplay = if (matchType == MatchType.IN_PLAY) INPLAY else 0,
                            leagueId = "",
                            leagueName = "",
                            matchId = matchOdd.matchInfo.id,
                            odds = odds,
                            hkOdds = hkOdds,
                            oddsId = oddsId,
                            playCateId = 0,
                            playCateName = playCateName,
                            playCode = "",
                            playId = 0,
                            playName = odd.name ?: "",
                            producerId = producerId,
                            spread = odd.spread ?: "",
                            startTime = matchOdd.matchInfo.startTime.toLong(),
                            status = odd.status,
                            gameType = gameType,
                            homeScore = matchOdd.matchInfo.homeScore ?: 0,
                            awayScore = matchOdd.matchInfo.awayScore ?: 0
                        )
                    }
                }
            }
        }

        return null
    }
}