package org.cxct.sportlottery.util

import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.global_stop.GlobalStopEvent
import org.cxct.sportlottery.network.service.match_clock.MatchClockEvent
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusChangeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.ui.odds.OddsDetailListData

object SocketUpdateUtil {

    fun updateMatchStatus(
        gameType: String?,
        matchOddList: MutableList<MatchOdd>,
        matchStatusChangeEvent: MatchStatusChangeEvent
    ): Boolean {
        var isNeedRefresh = false

        matchStatusChangeEvent.matchStatusCO?.let { matchStatusCO ->

            matchOddList.forEach { matchOdd ->

                if (matchStatusCO.matchId != null && matchStatusCO.matchId == matchOdd.matchInfo?.id) {

                    isNeedRefresh = when {
                        (matchStatusCO.status == 100) -> {
                            matchOddList.remove(matchOdd)
                            true
                        }

                        (matchStatusCO.homeScore != null && matchStatusCO.homeScore != matchOdd.matchInfo?.homeScore) -> {
                            matchOdd.matchInfo?.homeScore = matchStatusCO.homeScore
                            true
                        }

                        (matchStatusCO.awayScore != null && matchStatusCO.awayScore != matchOdd.matchInfo?.awayScore) -> {
                            matchOdd.matchInfo?.awayScore = matchStatusCO.awayScore
                            true
                        }

                        (matchStatusCO.statusName != null && matchStatusCO.statusName != matchOdd.matchInfo?.statusName) -> {
                            matchOdd.matchInfo?.statusName = matchStatusCO.statusName
                            true
                        }

                        (matchStatusCO.homeTotalScore != null && matchStatusCO.homeTotalScore != matchOdd.matchInfo?.homeTotalScore) -> {
                            matchOdd.matchInfo?.homeTotalScore = matchStatusCO.homeTotalScore
                            true
                        }

                        (matchStatusCO.awayTotalScore != null && matchStatusCO.awayTotalScore != matchOdd.matchInfo?.awayTotalScore) -> {
                            matchOdd.matchInfo?.awayTotalScore = matchStatusCO.awayTotalScore
                            true
                        }

                        (gameType == GameType.TN.key) -> {
                            when {
                                (matchStatusCO.homePoints != null && matchStatusCO.homePoints != matchOdd.matchInfo?.homePoints) -> {
                                    matchOdd.matchInfo?.homePoints = matchStatusCO.homePoints
                                    true
                                }
                                (matchStatusCO.awayPoints != null && matchStatusCO.awayPoints != matchOdd.matchInfo?.awayPoints) -> {
                                    matchOdd.matchInfo?.awayPoints = matchStatusCO.awayPoints
                                    true
                                }

                                else -> false
                            }
                        }

                        (gameType == GameType.FT.key) -> {
                            when {
                                //home
                                (matchStatusCO.homeCards != null && matchStatusCO.homeCards != matchOdd.matchInfo?.homeCards) -> {
                                    matchOdd.matchInfo?.homeCards = matchStatusCO.homeCards
                                    true
                                }

                                //away
                                (matchStatusCO.awayCards != null && matchStatusCO.awayCards != matchOdd.matchInfo?.awayCards) -> {
                                    matchOdd.matchInfo?.awayCards = matchStatusCO.awayCards
                                    true
                                }
                                else -> false
                            }
                        }


                        else -> false
                    }

                }
            }
        }

        return isNeedRefresh
    }

    fun updateMatchClock(matchOdd: MatchOdd, matchClockEvent: MatchClockEvent): Boolean {
        var isNeedRefresh = false

        matchClockEvent.matchClockCO?.let { matchClockCO ->

            if (matchClockCO.matchId != null && matchClockCO.matchId == matchOdd.matchInfo?.id) {

                val leagueTime = when (matchClockCO.gameType) {
                    GameType.FT.key -> {
                        matchClockCO.matchTime
                    }
                    GameType.BK.key -> {
                        matchClockCO.remainingTimeInPeriod
                    }
                    else -> null
                }

                isNeedRefresh = when {
                    (leagueTime != null && leagueTime != matchOdd.matchInfo?.leagueTime) -> {
                        matchOdd.matchInfo?.leagueTime = leagueTime
                        true
                    }
                    else -> false
                }
            }
        }

        return isNeedRefresh
    }

    fun updateMatchOdds(matchOdd: MatchOdd, oddsChangeEvent: OddsChangeEvent): Boolean {
        var isNeedRefresh = false

        if (oddsChangeEvent.eventId != null && oddsChangeEvent.eventId == matchOdd.matchInfo?.id) {

            isNeedRefresh = when (matchOdd.oddsMap.isNullOrEmpty()) {
                true -> {
                    insertMatchOdds(matchOdd, oddsChangeEvent)
                }

                false -> {
                    refreshMatchOdds(matchOdd.oddsMap, oddsChangeEvent) ||
                            matchOdd.quickPlayCateList?.any {
                                refreshMatchOdds(it.quickOdds ?: mutableMapOf(), oddsChangeEvent)
                            } ?: false
                }
            }
        }

        return isNeedRefresh
    }

    fun updateMatchOdds(
        oddsDetailListData: OddsDetailListData,
        matchOddsChangeEvent: MatchOddsChangeEvent
    ): Boolean {
        return when (oddsDetailListData.oddArrayList.isNullOrEmpty()) {
            true -> {
                insertMatchOdds(oddsDetailListData, matchOddsChangeEvent)
            }
            false -> {
                refreshMatchOdds(oddsDetailListData, matchOddsChangeEvent)
            }
        }
    }

    fun updateOddStatus(matchOdd: MatchOdd, globalStopEvent: GlobalStopEvent): Boolean {
        var isNeedRefresh = false

        matchOdd.oddsMap.values.forEach { odds ->
            odds.filter { odd ->
                globalStopEvent.producerId == null || globalStopEvent.producerId == odd?.producerId
            }.forEach { odd ->
                if (odd?.status != BetStatus.DEACTIVATED.code) {
                    odd?.status = BetStatus.DEACTIVATED.code
                    isNeedRefresh = true
                }
            }
        }

        return isNeedRefresh
    }

    fun updateOddStatus(
        oddsDetailListData: OddsDetailListData,
        globalStopEvent: GlobalStopEvent
    ): Boolean {
        var isNeedRefresh = false

        oddsDetailListData.oddArrayList.filter { odd ->
            globalStopEvent.producerId == null || globalStopEvent.producerId == odd?.producerId
        }.forEach { odd ->
            if (odd?.status != BetStatus.DEACTIVATED.code) {
                odd?.status = BetStatus.DEACTIVATED.code
                isNeedRefresh = true
            }
        }

        return isNeedRefresh
    }

    private fun insertMatchOdds(matchOdd: MatchOdd, oddsChangeEvent: OddsChangeEvent): Boolean {
        matchOdd.oddsMap = oddsChangeEvent.odds?.mapValues {
            it.value.toMutableList()
        }?.toMutableMap() ?: mutableMapOf()

        return oddsChangeEvent.odds?.isNotEmpty() ?: false
    }

    private fun insertMatchOdds(
        oddsDetailListData: OddsDetailListData,
        matchOddsChangeEvent: MatchOddsChangeEvent
    ): Boolean {
        val odds = matchOddsChangeEvent.odds?.get(matchOddsChangeEvent.odds.keys.find {
            it == oddsDetailListData.gameType
        })

        oddsDetailListData.oddArrayList = odds?.odds ?: listOf()

        return odds?.odds?.isNotEmpty() ?: false
    }

    private fun refreshMatchOdds(
        oddsMap: Map<String, List<Odd?>>,
        oddsChangeEvent: OddsChangeEvent
    ): Boolean {
        var isNeedRefresh = false

        oddsMap.forEach { oddTypeMap ->
            val oddsSocket = oddsChangeEvent.odds?.get(oddTypeMap.key)
            val odds = oddTypeMap.value

            odds.forEach { odd ->
                val oddSocket = oddsSocket?.find { oddSocket ->
                    oddSocket?.id == odd?.id
                }

                oddSocket?.let {
                    odd?.odds?.let { oddValue ->
                        oddSocket.odds?.let { oddSocketValue ->
                            when {
                                oddValue > oddSocketValue -> {
                                    odd.oddState =
                                        OddState.SMALLER.state

                                    isNeedRefresh = true
                                }
                                oddValue < oddSocketValue -> {
                                    odd.oddState =
                                        OddState.LARGER.state

                                    isNeedRefresh = true
                                }
                                oddValue == oddSocketValue -> {
                                    odd.oddState =
                                        OddState.SAME.state
                                }
                            }
                        }
                    }

                    odd?.odds = oddSocket.odds
                    odd?.hkOdds = oddSocket.hkOdds

                    if (odd?.status != oddSocket.status) {
                        odd?.status = oddSocket.status

                        isNeedRefresh = true
                    }

                    if (odd?.extInfo != oddSocket.extInfo) {
                        odd?.extInfo = oddSocket.extInfo

                        isNeedRefresh = true
                    }
                }
            }
        }

        return isNeedRefresh
    }

    private fun refreshMatchOdds(
        oddsDetailListData: OddsDetailListData,
        matchOddsChangeEvent: MatchOddsChangeEvent
    ): Boolean {
        var isNeedRefresh = false

        val odds = matchOddsChangeEvent.odds?.get(matchOddsChangeEvent.odds.keys.find {
            it == oddsDetailListData.gameType
        })

        oddsDetailListData.oddArrayList.forEach { odd ->
            val oddSocket = odds?.odds?.find { oddSocket ->
                oddSocket?.id == odd?.id
            }

            oddSocket?.let {
                odd?.odds?.let { oddValue ->
                    oddSocket.odds?.let { oddSocketValue ->
                        when {
                            oddValue > oddSocketValue -> {
                                odd.oddState =
                                    OddState.SMALLER.state

                                isNeedRefresh = true
                            }
                            oddValue < oddSocketValue -> {
                                odd.oddState =
                                    OddState.LARGER.state

                                isNeedRefresh = true
                            }
                            oddValue == oddSocketValue -> {
                                odd.oddState =
                                    OddState.SAME.state
                            }
                        }
                    }
                }

                odd?.odds = oddSocket.odds
                odd?.hkOdds = oddSocket.hkOdds

                if (odd?.status != oddSocket.status) {
                    odd?.status = oddSocket.status

                    isNeedRefresh = true
                }

                if (odd?.extInfo != oddSocket.extInfo) {
                    odd?.extInfo = oddSocket.extInfo

                    isNeedRefresh = true
                }
            }
        }

        return isNeedRefresh
    }
}