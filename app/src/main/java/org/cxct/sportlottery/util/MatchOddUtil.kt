package org.cxct.sportlottery.util

import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsOdd
import java.math.RoundingMode

object MatchOddUtil {
    fun transfer(
        matchType: MatchType,
        gameType: String,
        playCateCode: String,
        playCateName: String,
        playName: String?,
        matchInfo: MatchInfo,
        odd: Odd,
    ): MatchOdd? {
        matchInfo.id.let { matchId ->
            odd.id?.let { oddsId ->
                odd.odds?.let { odds ->
                    odd.hkOdds?.let { hkOdds ->
                        return MatchOdd(
                            awayName = matchInfo.awayName,
                            homeName = matchInfo.homeName,
                            inplay = if (matchType == MatchType.IN_PLAY) 1 else 0,
                            leagueId = "",
                            leagueName = matchInfo.leagueName,
                            matchId = matchId,
                            originalOdds = odd.originalOdds,
                            odds = odds,
                            hkOdds = hkOdds,
                            malayOdds = odd.malayOdds!!,
                            indoOdds = odd.indoOdds!!,
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
                            homeScore = matchInfo.homeScore?.toIntOrNull() ?: 0,
                            awayScore = matchInfo.awayScore?.toIntOrNull() ?: 0,
                            betPlayCateNameMap = null,
                            playCateNameMap = null,
                            matchInfo = null,
                            oddsMap = null,
                            oddsSort = null,
                            quickPlayCateList = null,
                            oddsEps = null,
                        ).apply {
                            extInfo = odd.extInfo
                            isOnlyEUType = odd.isOnlyEUType
                            homeCornerKicks = matchInfo.homeCornerKicks
                            awayCornerKicks = matchInfo.awayCornerKicks
                            categoryCode = matchInfo.categoryCode
                        }

                    }
                }
            }

        }
        return null
    }

    fun Double.applyDiscount(discount: Float): Double {
        return ArithUtil.round(
            ArithUtil.add(ArithUtil.mul(ArithUtil.sub(this, 1.0), discount.toDouble()), 1.0),
            2,
            RoundingMode.HALF_UP
        )
            .toDouble()
    }

    fun Double.applyHKDiscount(discount: Float): Double {
        return ArithUtil.round(ArithUtil.mul(this, discount.toDouble()), 2, RoundingMode.HALF_UP).toDouble()
    }

    fun Double.convertToMYOdds(): Double {

        return when {
            this == 0.0 -> {
                this
            }
            this > 1 -> {
                //(-1 / this)
                ArithUtil.div(-1.0,this,2, RoundingMode.DOWN)
            }
            else -> {
                this
            }
        }
    }
    fun Double.convertToIndoOdds(): Double {
        return when {
            this == 0.0 -> {
                this
            }
            this > 1 -> {
                this
            }
            else -> {
                //(-1 / this)
                ArithUtil.div(-1.0, this, 2, RoundingMode.DOWN)
            }
        }
    }

    fun Odd?.setupOddsDiscount(
        isLCS: Boolean,
        playCateCode: String?,
        discount: Float,
    ) {
        this.let { odd ->
            if (isLCS) { //反波膽不處理折扣
                val oddsDiscount = odd?.odds

                if (odd?.odds == odd?.hkOdds && odd?.odds == odd?.malayOdds && odd?.odds == odd?.indoOdds) {
                    odd?.odds = oddsDiscount
                    odd?.hkOdds = oddsDiscount
                    odd?.malayOdds = oddsDiscount
                    odd?.indoOdds = oddsDiscount
                } else {
                    odd?.odds = oddsDiscount
                    odd?.hkOdds = odd?.hkOdds
                    val hkOddsHalfUp =
                        ArithUtil.round(odd?.hkOdds, 2, RoundingMode.HALF_UP).toDouble()
                    odd?.malayOdds = hkOddsHalfUp.convertToMYOdds()
                    odd?.indoOdds = hkOddsHalfUp.convertToIndoOdds()
                }

                if (playCateCode == PlayCate.EPS.value) {
                    odd?.extInfo = odd?.extInfo?.toDouble().toString()
                } else {

                }
            } else {
                val oddsDiscount = odd?.odds?.applyDiscount(discount)

                if (odd?.odds == odd?.hkOdds && odd?.odds == odd?.malayOdds && odd?.odds == odd?.indoOdds) {
                    odd?.odds = oddsDiscount
                    odd?.hkOdds = oddsDiscount
                    odd?.malayOdds = oddsDiscount
                    odd?.indoOdds = oddsDiscount
                } else {
                    odd?.odds = oddsDiscount
                    odd?.hkOdds = odd?.hkOdds?.applyHKDiscount(discount)
                    val hkOddsHalfUp =
                        ArithUtil.round(odd?.hkOdds, 2, RoundingMode.HALF_UP).toDouble()
                    odd?.malayOdds = hkOddsHalfUp.convertToMYOdds()
                    odd?.indoOdds = hkOddsHalfUp?.convertToIndoOdds()
                }

                if (playCateCode == PlayCate.EPS.value) {
                    odd?.extInfo =
                        odd?.extInfo?.toDouble()?.applyDiscount(discount)?.toString()
                }
            }
        }
    }
}