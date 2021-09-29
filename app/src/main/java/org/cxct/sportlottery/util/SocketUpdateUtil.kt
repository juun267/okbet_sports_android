package org.cxct.sportlottery.util

import android.content.Context
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.common.QuickPlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.global_stop.GlobalStopEvent
import org.cxct.sportlottery.network.service.match_clock.MatchClockEvent
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.match_odds_lock.MatchOddsLockEvent
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusChangeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.ui.common.PlayCateMapItem
import org.cxct.sportlottery.ui.game.home.recommend.OddBean
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

                    if (matchStatusCO.status == 100) {
                        matchOddList.remove(matchOdd)
                        isNeedRefresh = true
                    }

                    if (matchStatusCO.status != matchOdd.matchInfo?.socketMatchStatus) {
                        matchOdd.matchInfo?.socketMatchStatus = matchStatusCO.status
                        isNeedRefresh = true
                    }

                    if (matchStatusCO.statusName != null && matchStatusCO.statusName != matchOdd.matchInfo?.statusName) {
                        matchOdd.matchInfo?.statusName = matchStatusCO.statusName
                        isNeedRefresh = true
                    }

                    if (matchStatusCO.homeScore != null && matchStatusCO.homeScore != matchOdd.matchInfo?.homeScore) {
                        matchOdd.matchInfo?.homeScore = matchStatusCO.homeScore
                        isNeedRefresh = true
                    }

                    if (matchStatusCO.awayScore != null && matchStatusCO.awayScore != matchOdd.matchInfo?.awayScore) {
                        matchOdd.matchInfo?.awayScore = matchStatusCO.awayScore
                        isNeedRefresh = true
                    }

                    if ((gameType == GameType.TN.key || gameType == GameType.VB.key) && matchStatusCO.homeTotalScore != null && matchStatusCO.homeTotalScore != matchOdd.matchInfo?.homeTotalScore) {
                        matchOdd.matchInfo?.homeTotalScore = matchStatusCO.homeTotalScore
                        isNeedRefresh = true
                    }

                    if ((gameType == GameType.TN.key || gameType == GameType.VB.key) && matchStatusCO.awayTotalScore != null && matchStatusCO.awayTotalScore != matchOdd.matchInfo?.awayTotalScore) {
                        matchOdd.matchInfo?.awayTotalScore = matchStatusCO.awayTotalScore
                        isNeedRefresh = true
                    }

                    if (gameType == GameType.TN.key && matchStatusCO.homePoints != null && matchStatusCO.homePoints != matchOdd.matchInfo?.homePoints) {
                        matchOdd.matchInfo?.homePoints = matchStatusCO.homePoints
                        isNeedRefresh = true
                    }

                    if (gameType == GameType.TN.key && matchStatusCO.awayPoints != null && matchStatusCO.awayPoints != matchOdd.matchInfo?.awayPoints) {
                        matchOdd.matchInfo?.awayPoints = matchStatusCO.awayPoints
                        isNeedRefresh = true
                    }

                    if (gameType == GameType.FT.key && matchStatusCO.homeCards != null && matchStatusCO.homeCards != matchOdd.matchInfo?.homeCards) {
                        matchOdd.matchInfo?.homeCards = matchStatusCO.homeCards
                        isNeedRefresh = true
                    }

                    if (gameType == GameType.FT.key && matchStatusCO.awayCards != null && matchStatusCO.awayCards != matchOdd.matchInfo?.awayCards) {
                        matchOdd.matchInfo?.awayCards = matchStatusCO.awayCards
                        isNeedRefresh = true
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

    /**
     * 根據賽事的oddsSort將盤口重新排序
     */
    private fun sortOdds(matchOdd: MatchOdd) {
        val sortOrder = matchOdd.oddsSort?.split(",")
        matchOdd.oddsMap = matchOdd.oddsMap.toSortedMap(compareBy<String> {
            val oddsIndex = sortOrder?.indexOf(it)
            oddsIndex
        }.thenBy { it })

    }

    fun updateMatchOdds(oddBean: OddBean, oddsChangeEvent: OddsChangeEvent): Boolean {
        val isNeedRefresh = when (oddBean.oddList.isNullOrEmpty()) {
            true -> {
                insertMatchOdds(oddBean, oddsChangeEvent)
            }
            false -> {
                refreshMatchOdds(
                    mutableMapOf(Pair(oddBean.playTypeCode, oddBean.oddList.toMutableList())),
                    oddsChangeEvent.odds,
                )
            }
        }

        if (isNeedRefresh) {
            oddBean.updateOddStatus()
        }

        return isNeedRefresh
    }

    fun updateMatchOdds(
        context: Context?,
        matchOdd: MatchOdd,
        oddsChangeEvent: OddsChangeEvent
    ): Boolean {
        var isNeedRefresh = false
        var isNeedRefreshPlayCate = false

        context?.let {
            if (oddsChangeEvent.eventId != null && oddsChangeEvent.eventId == matchOdd.matchInfo?.id) {
                val cateMenuCode =
                    oddsChangeEvent.channel?.split(context.getString(R.string.splash_no_trans))
                        ?.getOrNull(6)

                isNeedRefresh = when {
                    (cateMenuCode == PlayCate.EPS.value) -> {
                        updateMatchOdds(
                            mutableMapOf(
                                Pair(
                                    PlayCate.EPS.value,
                                    matchOdd.oddsEps?.eps?.toMutableList() ?: mutableListOf()
                                )
                            ),
                            oddsChangeEvent.odds,
                        )
                    }

                    (QuickPlayCate.values().map { it.value }.contains(cateMenuCode)) -> {
                        updateMatchOdds(
                            matchOdd.quickPlayCateList?.find { it.isSelected }?.quickOdds?.toMutableFormat()
                                ?: mutableMapOf(),
                            oddsChangeEvent.odds
                        )
                    }

                    else -> {
                        updateMatchOdds(
                            matchOdd.oddsMap,
                            oddsChangeEvent.odds,
                        )
                    }
                }

                isNeedRefreshPlayCate = when (matchOdd.quickPlayCateList.isNullOrEmpty()) {
                    true -> {
                        insertPlayCate(matchOdd, oddsChangeEvent)
                    }
                    false -> {
                        refreshPlayCate(matchOdd, oddsChangeEvent)
                    }
                }

                if (isNeedRefresh) {
                    sortOdds(matchOdd)
                    matchOdd.updateOddStatus()
                }
            }
        }

        return isNeedRefresh || isNeedRefreshPlayCate
    }

    private fun updateMatchOdds(
        oddsMap: MutableMap<String, MutableList<Odd?>?>,
        oddsMapSocket: Map<String, List<Odd?>?>?,
    ): Boolean {
        return when (oddsMap.isNullOrEmpty()) {
            true -> {
                insertMatchOdds(oddsMap, oddsMapSocket)
            }
            false -> {
                refreshMatchOdds(oddsMap, oddsMapSocket)
            }
        }
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
        }.apply {
            if (this) {
                oddsDetailListData.updateOddStatus()
            }
        }
    }

    fun updateOddStatus(oddBean: OddBean, globalStopEvent: GlobalStopEvent): Boolean {
        var isNeedRefresh = false

        oddBean.oddList.filter { odd ->
            globalStopEvent.producerId == null || globalStopEvent.producerId == odd?.producerId
        }.forEach { odd ->
            if (odd?.status != BetStatus.DEACTIVATED.code) {
                odd?.status = BetStatus.DEACTIVATED.code
                isNeedRefresh = true
            }
        }

        if (isNeedRefresh) {
            oddBean.updateOddStatus()
        }

        return isNeedRefresh
    }

    fun updateOddStatus(matchOdd: MatchOdd, globalStopEvent: GlobalStopEvent): Boolean {
        var isNeedRefresh = false

        matchOdd.oddsMap.values.forEach { odds ->
            odds?.filter { odd ->
                globalStopEvent.producerId == null || globalStopEvent.producerId == odd?.producerId
            }?.forEach { odd ->
                if (odd?.status != BetStatus.DEACTIVATED.code) {
                    odd?.status = BetStatus.DEACTIVATED.code
                    isNeedRefresh = true
                }
            }
        }

        matchOdd.oddsEps?.eps?.filter { odd -> globalStopEvent.producerId == null || globalStopEvent.producerId == odd?.producerId }
            ?.forEach { odd ->
                if (odd?.status != BetStatus.DEACTIVATED.code) {
                    odd?.status = BetStatus.DEACTIVATED.code
                    isNeedRefresh = true
                }
            }

        if (isNeedRefresh) {
            matchOdd.updateOddStatus()
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

        if (isNeedRefresh) {
            oddsDetailListData.updateOddStatus()
        }

        return isNeedRefresh
    }

    fun updateOddStatus(matchOdd: MatchOdd, matchOddsLockEvent: MatchOddsLockEvent): Boolean {
        var isNeedRefresh = false

        if (matchOdd.matchInfo?.id == matchOddsLockEvent.matchId) {
            matchOdd.oddsMap.values.forEach { odd ->
                odd?.forEach {
                    it?.status = BetStatus.LOCKED.code
                    isNeedRefresh = true
                }
            }

            matchOdd.oddsEps?.eps?.forEach { odd ->
                odd?.status = BetStatus.LOCKED.code
            }
        }

        return isNeedRefresh
    }

    private fun insertMatchOdds(oddBean: OddBean, oddsChangeEvent: OddsChangeEvent): Boolean {
        oddBean.oddList.toMutableList()
            .addAll(oddsChangeEvent.odds?.get(oddBean.playTypeCode) ?: listOf())

        return oddsChangeEvent.odds?.isNotEmpty() ?: false
    }

    private fun insertMatchOdds(
        oddsMap: MutableMap<String, MutableList<Odd?>?>,
        oddsMapSocket: Map<String, List<Odd?>?>?,
    ): Boolean {
        oddsMap.putAll(oddsMapSocket?.toMutableFormat() ?: mutableMapOf())

        return oddsMapSocket?.isNotEmpty() ?: false
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

    private fun insertPlayCate(matchOdd: MatchOdd, oddsChangeEvent: OddsChangeEvent): Boolean {
        matchOdd.quickPlayCateList = oddsChangeEvent.quickPlayCateList
        return oddsChangeEvent.quickPlayCateList?.isNotEmpty() ?: false
    }

    private fun refreshMatchOdds(
        oddsMap: MutableMap<String, MutableList<Odd?>?>,
        oddsMapSocket: Map<String, List<Odd?>?>?,
    ): Boolean {
        var isNeedRefresh = false

        oddsMapSocket?.forEach { oddsMapEntrySocket ->
            when (oddsMap.keys.contains(oddsMapEntrySocket.key)) {
                true -> {
                    oddsMap.forEach { oddTypeMap ->
                        val oddsSocket = oddsMapEntrySocket.value
                        val odds = oddTypeMap.value

                        oddsSocket?.forEach { oddSocket ->
                            when (odds?.map { it?.id }?.contains(oddSocket?.id)) {
                                true -> {
                                    val odd = odds.find { odd ->
                                        odd?.id == oddSocket?.id
                                    }

                                    odd?.odds?.let { oddValue ->
                                        oddSocket?.odds?.let { oddSocketValue ->
                                            when {
                                                oddValue > oddSocketValue -> {
                                                    odd.oddState = OddState.SMALLER.state

                                                    isNeedRefresh = true
                                                }
                                                oddValue < oddSocketValue -> {
                                                    odd.oddState = OddState.LARGER.state

                                                    isNeedRefresh = true
                                                }
                                                oddValue == oddSocketValue -> {
                                                    odd.oddState = OddState.SAME.state
                                                }
                                            }
                                        }
                                    }

                                    odd?.odds = oddSocket?.odds
                                    odd?.hkOdds = oddSocket?.hkOdds

                                    if (odd?.status != oddSocket?.status) {
                                        odd?.status = oddSocket?.status

                                        isNeedRefresh = true
                                    }

                                    if (odd?.extInfo != oddSocket?.extInfo) {
                                        odd?.extInfo = oddSocket?.extInfo

                                        isNeedRefresh = true
                                    }
                                }

                                false -> {
                                    odds.add(oddSocket)

                                    isNeedRefresh = true
                                }
                            }
                        }
                    }
                }

                false -> {
                    oddsMap[oddsMapEntrySocket.key] = oddsMapEntrySocket.value?.toMutableList()

                    isNeedRefresh = true
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

    private fun refreshPlayCate(matchOdd: MatchOdd, oddsChangeEvent: OddsChangeEvent): Boolean {
        var isNeedRefresh = false

        oddsChangeEvent.quickPlayCateList?.forEach { quickPlayCateSocket ->
            when (matchOdd.quickPlayCateList?.contains(quickPlayCateSocket)) {
                false -> {
                    matchOdd.quickPlayCateList?.toMutableList()?.add(quickPlayCateSocket)
                    isNeedRefresh = true
                }
            }
        }
        return isNeedRefresh
    }

    private fun Map<String, List<Odd?>?>.splitPlayCate(): Map<String, List<Odd?>?> {
        val splitMap = mutableMapOf<String, List<Odd?>?>()

        this.forEach { oddsMap ->
            when (oddsMap.key) {
                PlayCate.SINGLE_OU.value -> {

                    splitMap[PlayCate.SINGLE_OU_O.value] =
                        listOf(
                            oddsMap.value?.getOrNull(0),
                            oddsMap.value?.getOrNull(2),
                            oddsMap.value?.getOrNull(4)
                        )

                    splitMap[PlayCate.SINGLE_OU_U.value] =
                        listOf(
                            oddsMap.value?.getOrNull(1),
                            oddsMap.value?.getOrNull(3),
                            oddsMap.value?.getOrNull(5)
                        )
                }

                PlayCate.SINGLE_BTS.value -> {

                    splitMap[PlayCate.SINGLE_BTS_Y.value] =
                        listOf(
                            oddsMap.value?.getOrNull(0),
                            oddsMap.value?.getOrNull(2),
                            oddsMap.value?.getOrNull(4)
                        )

                    splitMap[PlayCate.SINGLE_BTS_N.value] =
                        listOf(
                            oddsMap.value?.getOrNull(1),
                            oddsMap.value?.getOrNull(3),
                            oddsMap.value?.getOrNull(5)
                        )
                }

                else -> {
                    splitMap[oddsMap.key] = oddsMap.value
                }
            }
        }

        return splitMap
    }

    private fun Map<String, List<Odd?>?>.filterPlayCateSpanned(
        gameType: String?,
        playCateMappingList: List<PlayCateMapItem>?
    ): Map<String, List<Odd?>?> {
        return this.mapValues { map ->
            val playCateMapItem = playCateMappingList?.find {
                it.gameType == gameType && it.playCateCode == map.key
            }

            map.value?.filterIndexed { index, _ ->
                index < playCateMapItem?.playCateNum ?: 0
            }
        }
    }

    private fun Map<String, List<Odd?>?>.sortPlayCate(context: Context?): Map<String, List<Odd?>?> {
        val sortMap = mutableMapOf<String, List<Odd?>?>()

        this.forEach { oddsMap ->
            if (context != null && oddsMap.key.contains(PlayCate.SINGLE.value)) {
                val oddList = oddsMap.value?.toMutableList()

                oddList?.indexOf(oddList.find {
                    it?.nameMap?.get(LanguageManager.Language.EN.key)
                        ?.split(context.getString(R.string.dash_no_trans))
                        ?.getOrNull(0)?.contains(context.getString(R.string.draw_no_trans))
                        ?: false
                }
                )?.let {
                    if (it >= 0) {
                        oddList.add(oddList.removeAt(it))
                    }
                }

                sortMap[oddsMap.key] = oddList
            } else {
                sortMap[oddsMap.key] = oddsMap.value
            }
        }

        return sortMap
    }

    private fun Map<String, List<Odd?>?>.toMutableFormat(): MutableMap<String, MutableList<Odd?>?> {
        return this.mapValues { map ->
            map.value?.toMutableList() ?: mutableListOf()
        }.toMutableMap()
    }

    private fun OddBean.updateOddStatus() {
        this.oddList.filterNotNull().forEach { odd ->

            odd.status = when {
                (this.oddList.filterNotNull()
                    .all { mOdd -> mOdd.status == null || mOdd.status == BetStatus.DEACTIVATED.code }) -> BetStatus.DEACTIVATED.code

                (this.oddList.filterNotNull()
                    .any { mOdd -> mOdd.status == null || mOdd.status == BetStatus.DEACTIVATED.code } && odd.status == BetStatus.DEACTIVATED.code) -> BetStatus.LOCKED.code

                else -> odd.status
            }
        }
    }

    private fun MatchOdd.updateOddStatus() {
        this.oddsMap.forEach {
            it.value?.filterNotNull()?.forEach { odd ->

                odd.status = when {
                    (it.value?.filterNotNull()
                        ?.all { mOdd -> mOdd.status == null || mOdd.status == BetStatus.DEACTIVATED.code }
                        ?: true) -> BetStatus.DEACTIVATED.code

                    (it.value?.filterNotNull()
                        ?.any { mOdd -> mOdd.status == null || mOdd.status == BetStatus.DEACTIVATED.code } ?: true && odd.status == BetStatus.DEACTIVATED.code) -> BetStatus.LOCKED.code

                    else -> odd.status
                }
            }
        }

        this.oddsEps?.eps?.filterNotNull()?.forEach { odd ->
            this.oddsEps?.eps?.let { oddList ->
                odd.status = when {
                    (oddList.filterNotNull()
                        .all { mOdd -> mOdd.status == null || mOdd.status == BetStatus.DEACTIVATED.code }) -> BetStatus.DEACTIVATED.code

                    (oddList.filterNotNull()
                        .any { mOdd -> mOdd.status == null || mOdd.status == BetStatus.DEACTIVATED.code } && odd.status == BetStatus.DEACTIVATED.code) -> BetStatus.LOCKED.code

                    else -> odd.status
                }
            }
        }

        this.quickPlayCateList?.forEach { quickPlayCate ->
            quickPlayCate.quickOdds?.forEach {
                it.value?.filterNotNull()?.forEach { odd ->
                    it.value?.let { oddList ->
                        odd.status = when {
                            (oddList.filterNotNull()
                                .all { mOdd -> mOdd.status == null || mOdd.status == BetStatus.DEACTIVATED.code }) -> BetStatus.DEACTIVATED.code

                            (oddList.filterNotNull()
                                .any { mOdd -> mOdd.status == null || mOdd.status == BetStatus.DEACTIVATED.code } && odd.status == BetStatus.DEACTIVATED.code) -> BetStatus.LOCKED.code

                            else -> odd.status
                        }
                    }
                }
            }
        }
    }

    private fun OddsDetailListData.updateOddStatus() {
        this.oddArrayList.filterNotNull().forEach { odd ->

            odd.status = when {
                (this.oddArrayList.filterNotNull()
                    .all { mOdd -> mOdd.status == null || mOdd.status == BetStatus.DEACTIVATED.code }) -> BetStatus.DEACTIVATED.code

                (this.oddArrayList.filterNotNull()
                    .any { mOdd -> mOdd.status == null || mOdd.status == BetStatus.DEACTIVATED.code } && odd.status == BetStatus.DEACTIVATED.code) -> BetStatus.LOCKED.code

                else -> odd.status
            }
        }
    }
}