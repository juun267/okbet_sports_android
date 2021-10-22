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
import org.cxct.sportlottery.ui.game.home.recommend.OddBean
import org.cxct.sportlottery.ui.odds.OddsDetailListData

object SocketUpdateUtil {

    fun updateMatchStatus(
        gameType: String?,
        matchOddList: MutableList<MatchOdd>,
        matchStatusChangeEvent: MatchStatusChangeEvent,
        context: Context?
    ): Boolean {
        var isNeedRefresh = false

        matchStatusChangeEvent.matchStatusCO?.let { matchStatusCO ->

            matchOddList.forEach { matchOdd ->

                if (matchStatusCO.matchId != null && matchStatusCO.matchId == matchOdd.matchInfo?.id) {

                    if (matchStatusCO.status == 100) {
                        val matchOddIterator = matchOddList.iterator()
                        while (matchOddIterator.hasNext()){
                            val item = matchOddIterator.next()
                            if (item == matchOdd)
                                matchOddIterator.remove()
                        }
                        isNeedRefresh = true
                    }

                    if (matchStatusCO.status != matchOdd.matchInfo?.socketMatchStatus) {
                        matchOdd.matchInfo?.socketMatchStatus = matchStatusCO.status
                        isNeedRefresh = true
                    }

                    if (matchStatusCO.statusName != null && matchStatusCO.statusName != matchOdd.matchInfo?.statusName18n) {
                        val statusValue = matchStatusCO.statusNameI18n?.get(LanguageManager.getSelectLanguage(context).key) ?: matchStatusCO.statusName
                        matchOdd.matchInfo?.statusName18n = statusValue
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
        val oddsMap = matchOdd.oddsMap.toSortedMap(compareBy<String> {
            val oddsIndex = sortOrder?.indexOf(it)
            oddsIndex
        }.thenBy { it })

        matchOdd.oddsMap.clear()
        matchOdd.oddsMap.putAll(oddsMap)
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

    /**
     * 加入新增的玩法並同時更新已有玩法的資料再以rowSort排序
     */
    fun updateMatchOddsMap(
        oddsDetailDataList: ArrayList<OddsDetailListData>,
        matchOddsChangeEvent: MatchOddsChangeEvent
    ): ArrayList<OddsDetailListData>? {
        //若有新玩法的話需要重新setData
        var addedNewOdds = false

        //新玩法
        val newOdds = matchOddsChangeEvent.odds?.filter { socketOdds ->
            oddsDetailDataList.find { it.gameType == socketOdds.key } == null
        }

        val newOddsDetailDataList: ArrayList<OddsDetailListData> = ArrayList()
        newOddsDetailDataList.addAll(oddsDetailDataList)

        //加入新玩法
        newOdds?.forEach { (key, value) ->
            val filteredOddList =
                mutableListOf<Odd?>()
            value.odds?.forEach { detailOdd ->
                //因排版問題 null也需要添加
                filteredOddList.add(detailOdd)
            }
            newOddsDetailDataList.add(
                OddsDetailListData(
                    key,
                    TextUtil.split(value.typeCodes),
                    value.name,
                    filteredOddList,
                    value.nameMap,
                    value.rowSort
                )
            )
            addedNewOdds = true
        }

        newOddsDetailDataList.apply {
            if (addedNewOdds){
                forEach { oddsDetailListData ->
                    updateMatchOdds(oddsDetailListData, matchOddsChangeEvent)
                }
            }
            sortBy { it.rowSort }
            //因UI需求 特優賠率移到第一項
            find { it.gameType == PlayCate.EPS.value }?.also { oddsDetailListData ->
                add(0, removeAt(indexOf(oddsDetailListData)))
            }
        }

        return if (addedNewOdds) newOddsDetailDataList else null
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
        matchOdd.quickPlayCateList?.addAll(oddsChangeEvent.quickPlayCateList ?: listOf())
        return oddsChangeEvent.quickPlayCateList?.isNotEmpty() ?: false
    }

    private fun refreshMatchOdds(
        oddsMap: MutableMap<String, MutableList<Odd?>?>,
        oddsMapSocket: Map<String, List<Odd?>?>?,
    ): Boolean {
        var isNeedRefresh = false

        oddsMapSocket?.forEach { oddsMapEntrySocket ->
            //全null : 有玩法沒賠率資料
            when (oddsMap.keys.contains(oddsMapEntrySocket.key) && oddsMap[oddsMapEntrySocket.key]?.all { it == null } == false) {
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

                                    if (odd?.spread != oddSocket?.spread) {
                                        odd?.spread = oddSocket?.spread

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

                if (oddsDetailListData.rowSort != odds.rowSort){
                    oddsDetailListData.rowSort = odds.rowSort

                    isNeedRefresh = true
                }
            }
        }

        return isNeedRefresh
    }

    private fun refreshPlayCate(matchOdd: MatchOdd, oddsChangeEvent: OddsChangeEvent): Boolean {
        var isNeedRefresh = false

        oddsChangeEvent.quickPlayCateList?.forEach { quickPlayCateSocket ->
            when (matchOdd.quickPlayCateList?.map { it.code }?.contains(quickPlayCateSocket.code)) {
                false -> {
                    matchOdd.quickPlayCateList?.add(quickPlayCateSocket)
                    isNeedRefresh = true
                }
            }
        }
        return isNeedRefresh
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