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
                        }

                    }
                }
            }

        }
        return null
    }
    fun MutableMap<String, MutableList<Odd>?>.updateOddsDiscount(discount: Float, newDiscount: Float) {
        this.toMap().forEach { (_, value) ->
            value?.toList()?.forEach { odd ->
                if (!keys.contains(PlayCate.LCS.value)) odd?.updateDiscount(discount, newDiscount)
            }
        }
    }

    fun EpsOdd.updateEpsDiscount(discount: Float, newDiscount: Float) {
        this.eps?.forEach { odd ->
            odd?.updateEPSDiscount(discount, newDiscount)
        }
    }

    fun Odd.updateDiscount(discount: Float, newDiscount: Float) {
        val oddsDiscount = this.hkOdds?.updateHKDiscount(discount, newDiscount)
        this.odds = oddsDiscount

        if (isOnlyEUType) {
            this.hkOdds = oddsDiscount
            this.malayOdds = oddsDiscount
            this.indoOdds = oddsDiscount
        } else {
            this.hkOdds = this.hkOdds?.updateHKDiscount(discount, newDiscount)
            if (this.malayOdds != this.odds) {
                this.malayOdds = this.hkOdds?.convertToMYOdds()
                this.indoOdds = this.hkOdds?.convertToIndoOdds()
            }
        }

        this.hkOdds = this.hkOdds?.updateHKDiscount(discount, newDiscount)
        if(this.malayOdds != this.odds){
            this.malayOdds = this.hkOdds?.convertToMYOdds()
            this.indoOdds = this.hkOdds?.convertToIndoOdds()
        }
    }

    fun Odd.updateEPSDiscount(discount: Float, newDiscount: Float) {
        this.updateDiscount(discount, newDiscount)
        this.extInfo = this.extInfo?.toDouble()?.updateDiscount(discount, newDiscount)?.toString()
    }

    fun MatchOdd.updateDiscount(discount: Float, newDiscount: Float) {
        if (playCode == PlayCate.LCS.value) return
        this.odds = this.odds.updateDiscount(discount, newDiscount)
        this.hkOdds = this.hkOdds.updateHKDiscount(discount, newDiscount)
        if(this.malayOdds != this.odds){
            this.malayOdds = this.hkOdds?.convertToMYOdds()
            this.indoOdds = this.hkOdds?.convertToIndoOdds()
        }

        if (this.playCode == PlayCate.EPS.value) {
            this.extInfo = this.extInfo?.toDouble()?.updateDiscount(discount, newDiscount)?.toString()
        }
    }

    fun Double.applyDiscount(discount: Float?): Double {
        return ArithUtil.round(
            ArithUtil.add(ArithUtil.mul(ArithUtil.sub(this, 1.0), discount?.toDouble()?:1.0), 1.0),
            2,
            RoundingMode.HALF_UP
        )
            .toDouble()
    }

    fun Double.applyHKDiscount(discount: Float?): Double {
        return ArithUtil.round(ArithUtil.mul(this, discount?.toDouble()?:1.0), 2, RoundingMode.HALF_UP).toDouble()
    }

    private fun Double.applyReverselyDiscount(discount: Float): Double {
        return ArithUtil.add(ArithUtil.div(ArithUtil.sub(this, 1.0), discount.toDouble()), 1.0)
    }

    private fun Double.applyReverselyHKDiscount(discount: Float): Double {
        return ArithUtil.div(this, discount.toDouble())
    }

    private fun Double.updateDiscount(discount: Float, newDiscount: Float): Double {
        return this.applyReverselyDiscount(discount).applyDiscount(newDiscount)
    }

    private fun Double.updateHKDiscount(discount: Float, newDiscount: Float): Double {
        return this.applyReverselyHKDiscount(discount).applyHKDiscount(newDiscount)
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
}