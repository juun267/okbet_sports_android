package org.cxct.sportlottery.util

import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.Odds
import org.cxct.sportlottery.ui.bet.list.INPLAY

object MatchOddUtil {
    fun transfer(
        matchType: MatchType,
        gameType: String,
        playCateCode: String,
        playCateName: String,
        playName: String?,
        matchInfo: MatchInfo,
        odd: Odd
    ): org.cxct.sportlottery.network.bet.info.MatchOdd? {
        matchInfo.id.let { matchId ->
            odd.id?.let { oddsId ->
                odd.odds?.let { odds ->
                    odd.hkOdds?.let { hkOdds ->
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
                            playCode = playCateCode,
                            playId = 0,
                            playName = playName ?: "",
                            producerId = odd.producerId ?: 0,
                            spread = odd.spread ?: "",
                            startTime = matchInfo.startTime,
                            status = odd.status,
                            gameType = gameType,
                            homeScore = matchInfo.homeScore ?: 0,
                            awayScore = matchInfo.awayScore ?: 0
                        ).apply {
                            extInfo = odd.extInfo
                        }

                    }
                }
            }

        }
        return null
    }

    fun MutableMap<String, MutableList<Odd?>?>.updateOddsDiscount(discount: Float, newDiscount: Float) {
        this.forEach { (_, value) ->
            value?.forEach { odd ->
                odd?.odds = odd?.odds?.updateDiscount(discount, newDiscount)
                odd?.hkOdds = odd?.hkOdds?.updateHKDiscount(discount, newDiscount)
            }
        }
    }

    fun Odds.updateEpsDiscount(discount: Float, newDiscount: Float) {
        this.eps?.forEach { odd ->
            odd?.odds = odd?.odds?.updateDiscount(discount, newDiscount)
            odd?.hkOdds = odd?.hkOdds?.updateHKDiscount(discount, newDiscount)
            odd?.extInfo = odd?.extInfo?.toDouble()?.updateDiscount(discount, newDiscount)?.toString()
        }
    }

    fun Double.applyDiscount(discount: Float): Double {
        return (this - 1).times(discount) + 1
    }

    fun Double.applyHKDiscount(discount: Float): Double {
        return this.times(discount)
    }

    private fun Double.applyReverselyDiscount(discount: Float): Double {
        return (this - 1).div(discount) + 1
    }

    private fun Double.applyReverselyHKDiscount(discount: Float): Double {
        return this.div(discount)
    }

    private fun Double.updateDiscount(discount: Float, newDiscount: Float): Double {
        return this.applyReverselyDiscount(discount).applyDiscount(newDiscount)
    }

    private fun Double.updateHKDiscount(discount: Float, newDiscount: Float): Double {
        return this.applyReverselyHKDiscount(discount).applyHKDiscount(newDiscount)
    }
}