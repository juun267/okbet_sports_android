package org.cxct.sportlottery.util

import android.content.Context
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.network.common.*
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
    @Synchronized
    fun updateMatchStatus(
        gameType: String?,
        matchOddList: MutableList<MatchOdd> = arrayListOf(),
        matchStatusChangeEvent: MatchStatusChangeEvent,
        context: Context?
    ): Boolean {
        var isNeedRefresh = false

        matchStatusChangeEvent.matchStatusCO?.let { matchStatusCO ->

            matchOddList.forEach { matchOdd ->

                if (matchStatusCO.matchId != null && matchStatusCO.matchId == matchOdd.matchInfo?.id) {

                    if (matchStatusCO.status != matchOdd.matchInfo?.socketMatchStatus) {
                        matchOdd.matchInfo?.socketMatchStatus = matchStatusCO.status
                        isNeedRefresh = true
                    }
                    val statusValue =
                        matchStatusCO.statusNameI18n?.get(LanguageManager.getSelectLanguage(context).key)
                            ?: matchStatusCO.statusName
                    if (statusValue != null && statusValue != matchOdd.matchInfo?.statusName18n) {

                        matchOdd.matchInfo?.statusName18n = statusValue
                        isNeedRefresh = true
                    }
                    if (matchStatusCO.gameType == GameType.CK.key) {

                        val homeTotal = matchStatusCO.homeTotalScore ?: 0
                        val homeOut = matchStatusCO.homeOut ?: 0
                        if (matchStatusCO.homeScore != null && matchStatusCO.homeScore.toString() != "${homeTotal}/${homeOut}") {
                            matchOdd.matchInfo?.homeScore = "${homeTotal}/${homeOut}"
                            isNeedRefresh = true
                        }


                        val awayTotal = matchStatusCO.awayTotalScore ?: 0
                        val awayOut = matchStatusCO.awayOut ?: 0
                        if (matchStatusCO.awayScore != null && matchStatusCO.awayScore.toString() != "${awayTotal}/${awayOut}") {
                            matchOdd.matchInfo?.awayScore = "${awayTotal}/${awayOut}"
                            isNeedRefresh = true
                        }
                    } else {
                        if (matchStatusCO.homeScore != null && matchStatusCO.homeScore.toString() != matchOdd.matchInfo?.homeScore) {
                            matchOdd.matchInfo?.homeScore = "${matchStatusCO.homeScore}"
                            isNeedRefresh = true
                        }
                        if (matchStatusCO.awayScore != null && matchStatusCO.awayScore.toString() != matchOdd.matchInfo?.awayScore) {
                            matchOdd.matchInfo?.awayScore = "${matchStatusCO.awayScore}"
                            isNeedRefresh = true
                        }
                    }


                    if (needUpdateTotalScore(gameType) && matchStatusCO.homeTotalScore != null && matchStatusCO.homeTotalScore != matchOdd.matchInfo?.homeTotalScore) {
                        matchOdd.matchInfo?.homeTotalScore = matchStatusCO.homeTotalScore
                        isNeedRefresh = true
                    }

                    if (needUpdateTotalScore(gameType) && matchStatusCO.awayTotalScore != null && matchStatusCO.awayTotalScore != matchOdd.matchInfo?.awayTotalScore) {
                        matchOdd.matchInfo?.awayTotalScore = matchStatusCO.awayTotalScore
                        isNeedRefresh = true
                    }

                    if (needUpdatePoints(gameType) && matchStatusCO.homePoints != null && matchStatusCO.homePoints != matchOdd.matchInfo?.homePoints) {
                        matchOdd.matchInfo?.homePoints = matchStatusCO.homePoints
                        isNeedRefresh = true
                    }

                    if (needUpdatePoints(gameType) && matchStatusCO.awayPoints != null && matchStatusCO.awayPoints != matchOdd.matchInfo?.awayPoints) {
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
            //matchStatusChange status = 100時，賽事結束
            if (matchStatusCO.status == GameMatchStatus.FINISH.value) {
                matchOddList.find { it.matchInfo?.id == matchStatusCO.matchId }.apply {
                    isNeedRefresh = this != null
                    this?.let {
                        matchOddList.remove(this)
                    }
                }
            }
        }
        return isNeedRefresh
    }

    private fun needUpdateTotalScore(gameType: String?) = when (gameType) {
        GameType.TN.key, GameType.VB.key, GameType.TT.key, GameType.BM.key -> true
        else -> false
    }

    private fun needUpdatePoints(gameType: String?) = when (gameType) {
        GameType.TN.key, GameType.VB.key -> true
        else -> false
    }

    fun updateMatchClock(matchOdd: MatchOdd, matchClockEvent: MatchClockEvent): Boolean {
        var isNeedRefresh = false

        matchClockEvent.matchClockCO?.let { matchClockCO ->

            if (matchClockCO.matchId != null && matchClockCO.matchId == matchOdd.matchInfo?.id) {

                val leagueTime = when (matchClockCO.gameType) {
                    GameType.FT.key -> {
                        matchClockCO.matchTime
                    }
                    GameType.BK.key,GameType.RB.key,GameType.AFT.key  -> {
                        matchClockCO.remainingTimeInPeriod
                    }
                    else -> null
                }


                if (leagueTime != null && leagueTime.toInt() != matchOdd.matchInfo?.leagueTime) {
                    matchOdd.matchInfo?.leagueTime = leagueTime.toInt()
                    isNeedRefresh = true
                }


                if (matchClockCO.stopped != matchOdd.matchInfo?.stopped) {
                    matchOdd.matchInfo?.stopped = matchClockCO.stopped
                    isNeedRefresh = true

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
        matchOdd.oddsMap?.let { oddMap ->
            val oddsMap = oddMap.toSortedMap(compareBy<String> {
                val oddsIndex = sortOrder?.indexOf(it)
                oddsIndex
            }.thenBy { it })

            oddMap.clear()
            oddMap.putAll(oddsMap)
        }
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

                    (cateMenuCode == PlayCate.OUTRIGHT.value) -> {
                        var updated = false
                        oddsChangeEvent.odds?.forEach { (key, value) ->
                            matchOdd.oddsMap?.let { oddsMap ->
                                if (oddsMap.containsKey(key)) {
                                    if (updateMatchOdds(
                                            mutableMapOf(
                                                Pair(
                                                    key, oddsMap[key]
                                                )
                                            ), mutableMapOf(Pair(key, value))
                                        )
                                    ) updated = true
                                } else {
                                    oddsMap[key] = value?.toMutableList()
                                    updated = true
                                }
                            }
                        }
                        updated
                    }

                    (QuickPlayCate.values().map { it.value }.contains(cateMenuCode)) -> {
                        updateMatchOdds(
                            matchOdd.quickPlayCateList?.find { it.isSelected }?.quickOdds?.toMutableFormat()
                                ?: mutableMapOf(),
                            oddsChangeEvent.odds
                        )
                    }

                    else -> {
                        if(matchOdd.oddsMap == null){
                            matchOdd.oddsMap = mutableMapOf()
                        }
                        updateMatchOdds(
                            matchOdd.oddsMap ?: mutableMapOf(),
                            oddsChangeEvent.odds,
                        )
                    }
                }

                //更新翻譯
                if(matchOdd.betPlayCateNameMap == null){
                    matchOdd.betPlayCateNameMap = mutableMapOf()
                }
                updateBetPlayCateNameMap(
                    matchOdd.betPlayCateNameMap,
                    oddsChangeEvent.betPlayCateNameMap
                )
                if(matchOdd.playCateNameMap == null){
                    matchOdd.playCateNameMap = mutableMapOf()
                }
                updatePlayCateNameMap(
                    matchOdd.playCateNameMap,
                    oddsChangeEvent.playCateNameMap
                )

                isNeedRefreshPlayCate = when (matchOdd.quickPlayCateList.isNullOrEmpty()) {
                    true -> {
                        insertPlayCate(matchOdd, oddsChangeEvent)
                    }
                    false -> {
                        refreshPlayCate(matchOdd, oddsChangeEvent)
                    }
                } || (matchOdd.matchInfo?.playCateNum != oddsChangeEvent.playCateNum)


                if (isNeedRefresh) {
                    sortOdds(matchOdd)
                    matchOdd.updateOddStatus()
                }

                if (isNeedRefreshPlayCate) {
                    matchOdd.matchInfo?.playCateNum = oddsChangeEvent.playCateNum
                }
            }
        }

        return isNeedRefresh || isNeedRefreshPlayCate
    }

    fun updateMatchOdds(oddsChangeEvent:OddsChangeEvent){
        oddsChangeEvent.odds = mutableMapOf()
        oddsChangeEvent.odds = oddsChangeEvent.oddsList.associateBy (keySelector= {it.playCateCode.toString()}, valueTransform={it.oddsList}).toMutableMap()
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

    private fun updateBetPlayCateNameMap(
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        betPlayCateNameMapSocket: Map<String?, Map<String?, String?>?>?,
    ) {
        betPlayCateNameMapSocket?.forEach {
            betPlayCateNameMap?.set(it.key, it.value)
        }
    }

    private fun updatePlayCateNameMap(
        playCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        playCateNameMapSocket: Map<String?, Map<String?, String?>?>?
    ) {
        playCateNameMapSocket?.forEach {
            playCateNameMap?.set(it.key, it.value)
        }
    }

    /**
     * 加入新增的玩法並同時更新已有玩法的資料再以rowSort排序
     */
    fun updateMatchOddsMap(
        oddsDetailDataList: ArrayList<OddsDetailListData>,
        matchOddsChangeEvent: MatchOddsChangeEvent,
        playCate: org.cxct.sportlottery.network.myfavorite.PlayCate?
    ): ArrayList<OddsDetailListData>? {
        //若有新玩法的話需要重新setData
        var addedNewOdds = false

        val newOddsDetailDataList: ArrayList<OddsDetailListData> = ArrayList()
        newOddsDetailDataList.addAll(oddsDetailDataList)

        //有新賠率盤口
        matchOddsChangeEvent.odds?.forEach { (key, value) ->
            oddsDetailDataList.filter { it.gameType == key }.forEach {
                val dataOddsList = it.oddArrayList
                val socketOddsList = value.odds

                //賠率id list
                val dataGroupByList = dataOddsList.map { odd -> odd?.id }
                val socketGroupByList = socketOddsList?.map { odd -> odd?.id } ?: listOf()

                //新的Odd
                val newOddsId = socketGroupByList.filter { socketId ->
                    !dataGroupByList.contains(socketId)
                }

                newOddsId.forEach { newOddId ->
                    socketOddsList?.find { socketOdd ->
                        socketOdd?.let { socketOddNotNull ->
                            socketOddNotNull.id == newOddId
                        } ?: false
                    }?.let { newOdd ->
                        it.oddArrayList.add(newOdd)
                        addedNewOdds = true
                    }
                }
            }
        }

        //新玩法
        val newPlay = matchOddsChangeEvent.odds?.filter { socketOdds ->
            oddsDetailDataList.find { it.gameType == socketOdds.key } == null
        }

        //加入新玩法
        newPlay?.forEach { (key, value) ->
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
            if (addedNewOdds) {
                forEach { oddsDetailListData ->
                    updateMatchOdds(oddsDetailListData, matchOddsChangeEvent)
                }
            }
            sortBy { it.rowSort }
            //因UI需求 特優賠率移到第一項
            find { it.gameType == PlayCate.EPS.value }?.also { oddsDetailListData ->
                add(0, removeAt(indexOf(oddsDetailListData)))
            }
            setupPinList(playCate)
        }

        return if (addedNewOdds) newOddsDetailDataList else null
    }

    /**
     * 重新配置已錠選的玩法位置
     */
    private fun ArrayList<OddsDetailListData>.setupPinList(playCate: org.cxct.sportlottery.network.myfavorite.PlayCate?) {
        val playCateCodeList = playCate?.code?.let { playCateString ->
            if (playCateString.isNotEmpty()) {
                TextUtil.split(playCateString).toList()
            } else {
                listOf()
            }
        }

        val pinList = this.filter { playCateCodeList?.contains(it.gameType) ?: false }
            .sortedByDescending { oddsDetailListData -> playCateCodeList?.indexOf(oddsDetailListData.gameType) }

        val epsSize = this.groupBy {
            it.gameType == PlayCate.EPS.value
        }[true]?.size ?: 0

        this.sortBy { it.originPosition }
        this.forEach { it.isPin = false }

        pinList.forEach { pinOddsDetailData ->
            pinOddsDetailData.isPin = true

            add(epsSize, this.removeAt(this.indexOf(pinOddsDetailData)))
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

        matchOdd.oddsMap?.values?.forEach { odds ->
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
            matchOdd.oddsMap?.values?.forEach { odd ->
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

        oddsDetailListData.oddArrayList = odds?.odds ?: mutableListOf()

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
                                    odd?.malayOdds = oddSocket?.malayOdds
                                    odd?.indoOdds = oddSocket?.indoOdds

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
                                    if (oddTypeMap.key == oddsMapEntrySocket.key)
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
                odd?.malayOdds = oddSocket.malayOdds
                odd?.indoOdds = oddSocket.indoOdds

                if (odd?.status != oddSocket.status) {
                    odd?.status = oddSocket.status

                    isNeedRefresh = true
                }

                if (odd?.spread != oddSocket.spread) {
                    odd?.spread = oddSocket.spread

                    isNeedRefresh = true
                }

                if (odd?.extInfo != oddSocket.extInfo) {
                    odd?.extInfo = oddSocket.extInfo

                    isNeedRefresh = true
                }

                if (oddsDetailListData.rowSort != odds.rowSort) {
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
        this.oddsMap?.forEach {
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
            quickPlayCate.quickOdds.forEach {
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