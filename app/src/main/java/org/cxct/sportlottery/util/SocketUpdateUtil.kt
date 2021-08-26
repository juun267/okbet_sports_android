package org.cxct.sportlottery.util

import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent

object SocketUpdateUtil {
    fun updateMatchOdds(matchOdd: MatchOdd, oddsChangeEvent: OddsChangeEvent): Boolean {
        var isNeedRefresh = false

        if (oddsChangeEvent.eventId == matchOdd.matchInfo?.id) {

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

    private fun insertMatchOdds(matchOdd: MatchOdd, oddsChangeEvent: OddsChangeEvent): Boolean {
        matchOdd.oddsMap = oddsChangeEvent.odds?.mapValues {
            it.value.toMutableList()
        }?.toMutableMap() ?: mutableMapOf()

        return oddsChangeEvent.odds?.isNotEmpty() ?: false
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
}