package org.cxct.sportlottery.util

import android.content.Context
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddState
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.match_odds_change.Odds
import org.cxct.sportlottery.network.service.match_status_change.MatchStatus
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.repository.GamePlayNameRepository
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.sport.detail.OddsDetailListData
import java.math.BigDecimal

object SocketUpdateUtil {
    /**
     * 从socket返回的数据，更新当前MatchOdd，并且返回是否需要刷新界面
     */
    fun updateMatchStatus(
        gameType: String?,
        matchOdd: MatchOdd,
        matchStatusChangeEvent: FrontWsEvent.MatchStatusChangeEvent,
        @Suppress("UNUSED_PARAMETER") context: Context?,
    ): Boolean {

        if (matchStatusChangeEvent.matchStatusCO == null) {
            return false
        }

        var isNeedRefresh = false
        val matchStatusCO = matchStatusChangeEvent.matchStatusCO
        if (matchStatusCO.matchId == null || matchStatusCO.matchId != matchOdd.matchInfo?.id) {
            return false
        }

        if (matchStatusCO.status != matchOdd.matchInfo?.socketMatchStatus) {
            matchOdd.matchInfo?.socketMatchStatus = matchStatusCO.status
            isNeedRefresh = true
        }
        val statusValue = GamePlayNameRepository.getStatusName(matchStatusCO.status)
        if (statusValue != matchOdd.matchInfo?.statusName18n) {
            matchOdd.matchInfo?.statusName18n = statusValue
            isNeedRefresh = true
        }

        //matchStatusList为空就用 periods
        val matchStatusList: MutableList<MatchStatus> = mutableListOf()
        if (matchStatusChangeEvent.matchStatusListList.isNotEmpty()) {
            matchStatusChangeEvent.matchStatusListList.forEach {
                matchStatusList.add(
                    MatchStatus(
                        homeScore = it.homeScore.toIntOrNull(),
                        awayScore = it.awayScore.toIntOrNull(),
                        homePoints = matchStatusCO.homePoints,
                        awayPoints = matchStatusCO.awayPoints,
                        homeCards = matchStatusCO.homeCards,
                        awayCards = matchStatusCO.awayCards,
                        statusCode = matchStatusCO.status,
                        statusName = GamePlayNameRepository.getStatusName(matchStatusCO.status),
                        statusNameI18n = GamePlayNameRepository.getMatchStatusResources(matchStatusCO.status)?.nameMap
                    )
                )
            }
        } else {
            matchStatusCO.periodsList.forEach {
                matchStatusList.add(
                    MatchStatus(
                        homeScore = it.homeScore.toIntOrNull(),
                        awayScore = it.awayScore.toIntOrNull(),
                        homePoints = matchStatusCO.homePoints,
                        awayPoints = matchStatusCO.awayPoints,
                        homeCards = it.homeCards.toIntOrNull(),
                        awayCards = it.awayCards.toIntOrNull(),
                        statusCode = it.status,
                        statusName = it.statusName,
                        statusNameI18n = GamePlayNameRepository.getMatchStatusResources(matchStatusCO.status)?.nameMap
                    )
                )
            }
        }
        matchOdd.matchInfo?.matchStatusList = matchStatusList

        if (matchStatusCO.gameType == GameType.CK.key) {
            val homeTotal = matchStatusCO.homeTotalScore ?: 0
            val homeOut = matchStatusCO.homeOut ?: 0
            if (matchStatusCO.homeScore.toString() != "${homeTotal}/${homeOut}") {
                matchOdd.matchInfo?.homeScore = "${homeTotal}/${homeOut}"
                isNeedRefresh = true
            }

            val awayTotal = matchStatusCO.awayTotalScore ?: 0
            val awayOut = matchStatusCO.awayOut ?: 0
            if (matchStatusCO.awayScore.toString() != "${awayTotal}/${awayOut}") {
                matchOdd.matchInfo?.awayScore = "${awayTotal}/${awayOut}"
                isNeedRefresh = true
            }
            if (matchStatusCO.homeOver != null && matchStatusCO.homeOver != matchOdd.matchInfo?.homeOver) {
                matchOdd.matchInfo?.homeOver = matchStatusCO.homeOver
                isNeedRefresh = true
            }
            if (matchStatusCO.awayOver != null && matchStatusCO.awayOver != matchOdd.matchInfo?.awayOver) {
                matchOdd.matchInfo?.awayOver = matchStatusCO.awayOver
                isNeedRefresh = true
            }
        } else {
            if (matchStatusCO.homeScore != null && matchStatusCO.homeScore.toString() != matchOdd.matchInfo?.homeScore) {
                matchOdd.matchInfo?.homeScore = matchStatusCO.homeScore
                isNeedRefresh = true
            }
            if (matchStatusCO.awayScore != null && matchStatusCO.awayScore.toString() != matchOdd.matchInfo?.awayScore) {
                matchOdd.matchInfo?.awayScore = matchStatusCO.awayScore
                isNeedRefresh = true
            }
        }

        if (needUpdateTotalScore(gameType) && matchStatusCO.homeTotalScore != null && matchStatusCO.homeTotalScore.toIntOrNull() != matchOdd.matchInfo?.homeTotalScore) {
            matchOdd.matchInfo?.homeTotalScore = matchStatusCO.homeTotalScore.toIntOrNull()
            isNeedRefresh = true
        }
        if (needUpdateTotalScore(gameType) && matchStatusCO.homeTotalScore != null && matchStatusCO.homeTotalScore.toIntOrNull() != matchOdd.matchInfo?.homeTotalScore) {
            matchOdd.matchInfo?.homeTotalScore = matchStatusCO.homeTotalScore.toIntOrNull()
            isNeedRefresh = true
        }

        if (needUpdateTotalScore(gameType) && matchStatusCO.awayTotalScore != null && matchStatusCO.awayTotalScore.toIntOrNull() != matchOdd.matchInfo?.awayTotalScore) {
            matchOdd.matchInfo?.awayTotalScore = matchStatusCO.awayTotalScore.toIntOrNull()
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
        if (gameType == GameType.FT.key && matchStatusCO.homeCornerKicks != matchOdd.matchInfo?.homeCornerKicks) {
            matchOdd.matchInfo?.homeCornerKicks = matchStatusCO.homeCornerKicks
            isNeedRefresh = true
        }
        if (gameType == GameType.FT.key && matchStatusCO.awayCornerKicks != matchOdd.matchInfo?.awayCornerKicks) {
            matchOdd.matchInfo?.awayCornerKicks = matchStatusCO.awayCornerKicks
            isNeedRefresh = true
        }
        if (gameType == GameType.FT.key && matchStatusCO.homeCards != matchOdd.matchInfo?.homeCards) {
            matchOdd.matchInfo?.homeCards = matchStatusCO.homeCards
            isNeedRefresh = true
        }

        if (gameType == GameType.FT.key && matchStatusCO.awayCards != matchOdd.matchInfo?.awayCards) {
            matchOdd.matchInfo?.awayCards = matchStatusCO.awayCards
            isNeedRefresh = true
        }
        if (gameType == GameType.FT.key && matchStatusCO.homeYellowCards != matchOdd.matchInfo?.homeYellowCards) {
            matchOdd.matchInfo?.homeYellowCards = matchStatusCO.homeYellowCards
            isNeedRefresh = true
        }

        if (gameType == GameType.FT.key && matchStatusCO.awayYellowCards != matchOdd.matchInfo?.awayYellowCards) {
            matchOdd.matchInfo?.awayYellowCards = matchStatusCO.awayYellowCards
            isNeedRefresh = true
        }
        if (gameType == GameType.FT.key && matchStatusCO.homeHalfScore != null && matchStatusCO.homeHalfScore != matchOdd.matchInfo?.homeHalfScore) {
            matchOdd.matchInfo?.homeHalfScore = matchStatusCO.homeHalfScore
            isNeedRefresh = true
        }

        if (gameType == GameType.FT.key && matchStatusCO.awayHalfScore != null && matchStatusCO.awayHalfScore != matchOdd.matchInfo?.awayHalfScore) {
            matchOdd.matchInfo?.awayHalfScore = matchStatusCO.awayHalfScore
            isNeedRefresh = true
        }

        if (needAttack(gameType) && matchStatusCO.attack != null && matchStatusCO.attack != matchOdd.matchInfo?.attack) {
            matchOdd.matchInfo?.attack = matchStatusCO.attack
            isNeedRefresh = true
        }
        if (gameType == GameType.BB.key && matchStatusCO.halfStatus != matchOdd.matchInfo?.halfStatus) {
            matchOdd.matchInfo?.halfStatus = matchStatusCO.halfStatus
            isNeedRefresh = true
        }
        if (gameType == GameType.BB.key && matchStatusCO.firstBaseBag != null && matchStatusCO.firstBaseBag.toIntOrNull() != matchOdd.matchInfo?.firstBaseBag) {
            matchOdd.matchInfo?.firstBaseBag = matchStatusCO.firstBaseBag.toIntOrNull()
            isNeedRefresh = true
        }
        if (gameType == GameType.BB.key && matchStatusCO.secBaseBag != null && matchStatusCO.secBaseBag.toIntOrNull() != matchOdd.matchInfo?.secBaseBag) {
            matchOdd.matchInfo?.secBaseBag = matchStatusCO.secBaseBag.toIntOrNull()
            isNeedRefresh = true
        }
        if (gameType == GameType.BB.key && matchStatusCO.thirdBaseBag != null && matchStatusCO.thirdBaseBag.toIntOrNull() != matchOdd.matchInfo?.thirdBaseBag) {
            matchOdd.matchInfo?.thirdBaseBag = matchStatusCO.thirdBaseBag.toIntOrNull()
            isNeedRefresh = true
        }
        if (gameType == GameType.BB.key && matchStatusCO.outNumber != null && matchStatusCO.outNumber.toIntOrNull() != matchOdd.matchInfo?.outNumber) {
            matchOdd.matchInfo?.outNumber = matchStatusCO.outNumber.toIntOrNull()
            isNeedRefresh = true
        }
        //matchStatusChange status = 100時，賽事結束
        if (matchStatusCO.status == GameMatchStatus.FINISH.value) {
            isNeedRefresh = true
        }
        return isNeedRefresh
    }

    private fun needUpdateTotalScore(gameType: String?) = when (gameType) {
        GameType.TN.key, GameType.VB.key, GameType.TT.key, GameType.BM.key, GameType.CK.key,GameType.BB.key -> true
        else -> false
    }

    private fun needUpdatePoints(gameType: String?) = when (gameType) {
        GameType.TN.key, GameType.VB.key -> true
        else -> false
    }

    private fun needAttack(gameType: String?) = when (gameType) {
        GameType.TN.key, GameType.VB.key, GameType.TT.key, GameType.BM.key, GameType.BB.key, GameType.CK.key, GameType.IH.key, -> true
        else -> false
    }

    fun updateMatchClock(matchOdd: MatchOdd, matchClockEvent: FrontWsEvent.MatchClockEvent): Boolean {
        return if (matchOdd.matchInfo == null) {
            false
        } else {
            updateMatchInfoClock(matchOdd.matchInfo!!, matchClockEvent)
        }
    }

    fun updateMatchClockStatus(matchInfo: MatchInfo, matchClockEvent: FrontWsEvent.MatchClockEvent): Boolean {
        val matchClockCO = matchClockEvent ?: return false

        var isNeedRefresh = false

        val leagueTime = if (matchClockCO.gameType == GameType.FT.key) {
            matchClockCO.matchTime
        } else if (matchClockCO.gameType == GameType.BK.key
            || matchClockCO.gameType == GameType.RB.key
            || matchClockCO.gameType ==GameType.AFT.key) {
            matchClockCO.remainingTimeInPeriod
        } else {
            null
        }

        if (leagueTime != null && leagueTime.toInt() != matchInfo.leagueTime) {
            matchInfo.leagueTime = leagueTime.toInt()
            isNeedRefresh = true
        }

        if (matchClockCO.stopped != matchInfo.stopped) {
            matchInfo.stopped = matchClockCO.stopped
            isNeedRefresh = true
        }

        return isNeedRefresh
    }

    private fun updateMatchInfoClock(matchInfo: MatchInfo, matchClockEvent: FrontWsEvent.MatchClockEvent): Boolean {
        var isNeedRefresh = false

        matchClockEvent.let { matchClockCO ->

            if (matchClockCO.matchId != null && matchClockCO.matchId == matchInfo.id) {

                val leagueTime = when (matchClockCO.gameType) {
                    GameType.FT.key -> {
                        matchClockCO.matchTime
                    }

                    GameType.BK.key, GameType.RB.key, GameType.AFT.key -> {
                        matchClockCO.remainingTimeInPeriod
                    }

                    else -> null
                }


                if (leagueTime != null && leagueTime.toInt() != matchInfo.leagueTime) {
                    matchInfo.leagueTime = leagueTime.toInt()
                    isNeedRefresh = true
                }


                if (matchClockCO.stopped != matchInfo.stopped) {
                    matchInfo?.stopped = matchClockCO.stopped
                    isNeedRefresh = true
                }
            }
        }
        return isNeedRefresh
    }

    fun updateMatchInfoClockByDetail(
        matchInfo: org.cxct.sportlottery.network.odds.MatchInfo,
        matchClockEvent: FrontWsEvent.MatchClockEvent,
    ): Boolean {
        var isNeedRefresh = false

        matchClockEvent.let { matchClockCO ->

            if (matchClockCO.matchId != null && matchClockCO.matchId == matchInfo.id) {

                val leagueTime = when (matchClockCO.gameType) {
                    GameType.FT.key -> {
                        matchClockCO.matchTime
                    }

                    GameType.BK.key, GameType.RB.key, GameType.AFT.key -> {
                        matchClockCO.remainingTimeInPeriod
                    }

                    else -> null
                }


                if (leagueTime != null && leagueTime.toInt() != matchInfo.leagueTime) {
                    matchInfo.leagueTime = leagueTime.toInt()
                    isNeedRefresh = true
                }


                if (matchClockCO.stopped != matchInfo.stopped) {
                    matchInfo?.stopped = matchClockCO.stopped
                    isNeedRefresh = true
                }
            }
        }
        return isNeedRefresh
    }

    /**
     * 根據賽事的oddsSort將盤口重新排序
     */
    fun sortOdds(matchOdd: MatchOdd) {
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

    fun updateMatchOdds(
        context: Context?,
        matchOdd: MatchOdd,
        oddsChangeEvent: OddsChangeEvent,
        matchType: MatchType? = null
    ): Boolean {

        if (context == null || oddsChangeEvent.eventId == null || oddsChangeEvent.eventId != matchOdd.matchInfo?.id) {
            return false
        }

        var isNeedRefresh = false
        var isNeedRefreshPlayCate = false

        val cateMenuCode =
            oddsChangeEvent.channel?.split(context.getString(R.string.splash_no_trans))
                ?.getOrNull(6)

        isNeedRefresh = when {

            (cateMenuCode == PlayCate.EPS.value) -> {
                updateMatchOdds(
                    mutableMapOf(
                        Pair(
                            PlayCate.EPS.value,
                            matchOdd.oddsEps?.eps?.toMutableList() ?: mutableListOf<Odd?>()
                        )
                    ),
                    oddsChangeEvent.odds,
                    oddsChangeEvent.updateMode
                )
            }

            (cateMenuCode == PlayCate.OUTRIGHT.value) -> {
                var updated = false
                oddsChangeEvent.odds?.forEach { (key, value) ->
                    matchOdd.oddsMap?.let { oddsMap ->
                        if (oddsMap.containsKey(key)) {
                            updated = updateMatchOdds(
                                mutableMapOf(Pair(key, oddsMap[key] as MutableList<Odd?>?)),
                                mutableMapOf(Pair(key, value)),
                                oddsChangeEvent.updateMode
                            )
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
                    matchOdd.quickPlayCateList?.find { it.isSelected }?.quickOdds?.toMutableFormat_1()
                        ?: mutableMapOf(),
                    oddsChangeEvent.odds,
                    oddsChangeEvent.updateMode
                )
            }

            else -> {
                if (matchOdd.oddsMap == null) {
                    matchOdd.oddsMap = mutableMapOf()
                }
                updateMatchOdds(matchOdd.oddsMap as MutableMap<String, MutableList<Odd?>?>? ?: mutableMapOf(), oddsChangeEvent.odds,oddsChangeEvent.updateMode)
            }
        }

        //更新翻譯
        if (matchOdd.betPlayCateNameMap == null) {
            matchOdd.betPlayCateNameMap = mutableMapOf()
        }

        updateBetPlayCateNameMap(matchOdd.betPlayCateNameMap, oddsChangeEvent.betPlayCateNameMap)

        if (matchOdd.playCateNameMap == null) {
            matchOdd.playCateNameMap = mutableMapOf()
        }

        updatePlayCateNameMap(matchOdd.playCateNameMap, oddsChangeEvent.playCateNameMap)

        isNeedRefreshPlayCate = when (matchOdd.quickPlayCateList.isNullOrEmpty()) {
            true -> {
                insertPlayCate(matchOdd, oddsChangeEvent, matchType)
            }

            false -> {
                refreshPlayCate(matchOdd, oddsChangeEvent, matchType)
            }
        }

        isNeedRefreshPlayCate =
            isNeedRefreshPlayCate || (matchOdd.matchInfo?.playCateNum != oddsChangeEvent.playCateNum)

        if (isNeedRefresh) {
            sortOdds(matchOdd)
            matchOdd.updateOddStatus()
        }

        if (isNeedRefreshPlayCate) {
            matchOdd.matchInfo?.playCateNum = oddsChangeEvent.playCateNum
        }

        return isNeedRefresh || isNeedRefreshPlayCate
    }

    fun updateMatchOdds(
        oddsMap: MutableMap<String, MutableList<Odd?>?>,
        oddsMapSocket: Map<String, List<Odd?>?>?,
        updateMode: Int?
    ): Boolean {
        return when (oddsMap.isNullOrEmpty()) {
            true -> {
                insertMatchOdds(oddsMap, oddsMapSocket)
            }

            false -> {
                refreshMatchOdds(oddsMap, oddsMapSocket, updateMode)
            }
        }
    }

    private fun insertMatchOdds(
        oddsMap: MutableMap<String, MutableList<Odd?>?>,
        oddsMapSocket: Map<String, List<Odd?>?>?,
    ): Boolean {

        oddsMap.putAll(oddsMapSocket?.toMutableFormat_1() ?: mapOf())

        return oddsMapSocket?.isNotEmpty() ?: false
    }

    private fun refreshMatchOdds(
        oddsMap: MutableMap<String, MutableList<Odd?>?>,
        oddsMapSocket: Map<String, List<Odd?>?>?,
        updateMode: Int?
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
                            val oddOld = odds?.firstOrNull { it?.id==oddSocket?.id }
                            when (isNeedUpdateOdd(oddSocket, oddOld)) {
                                true -> {
                                    val odd = odds?.find { odd ->
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

                                    //更新是不是只有歐洲盤 (因為棒球socket有機會一開始全部賠率推0.0)，跟後端(How)確認過目前只有棒球會這樣。
                                    odd?.isOnlyEUType =
                                        oddSocket?.odds == oddSocket?.hkOdds && oddSocket?.odds == oddSocket?.malayOdds && oddSocket?.odds == oddSocket?.indoOdds

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
                                    if (odd?.version != oddSocket?.version) {
                                        odd?.version = oddSocket?.version?:0

                                        isNeedRefresh = true
                                    }
                                }

                                false -> {
                                    if (oddTypeMap.key == oddsMapEntrySocket.key && oddSocket != null) odds?.add(
                                        oddSocket
                                    )

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
        //全量更新，在上面新增和修改后，再删除存在oddsMap中而不存在于oddsMapSocket中的玩法
        if (updateMode==2&&!oddsMapSocket.isNullOrEmpty()){
            val diffKeys = oddsMap.keys.subtract(oddsMapSocket.keys).toList()
            if (diffKeys.isNotEmpty()){
                diffKeys.forEach {
                    oddsMap.remove(it)
                }
                isNeedRefresh = true
            }
        }
        return isNeedRefresh
    }

    fun updateMatchOdds(
        oddsDetailListData: OddsDetailListData, matchOddsChangeEvent: MatchOddsChangeEvent
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

    fun updateBetPlayCateNameMap(
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        betPlayCateNameMapSocket: Map<String?, Map<String?, String?>?>?,
    ) {
        betPlayCateNameMapSocket?.forEach {
            betPlayCateNameMap?.set(it.key, it.value)
        }
    }

    fun updatePlayCateNameMap(
        playCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        playCateNameMapSocket: Map<String?, Map<String?, String?>?>?
    ) {
        playCateNameMapSocket?.forEach {
            playCateNameMap?.set(it.key, it.value)
        }
    }
//    fun updateMatchOddsMap(
//        oddsDetailMap: LinkedHashMap<String,Odds?>,
//        matchOddsChangeEvent: MatchOddsChangeEvent,
//    ): LinkedHashMap<String,MutableList<Odd?>?> {
//        val newOddsMap = matchOddsChangeEvent.odds?.filterValues { it.odds?.all { it?.status == BetStatus.DEACTIVATED.code }!=false }?: linkedMapOf()
//            //需要先移除失效的玩法
//        val removekeys = oddsDetailMap?.keys.subtract(newOddsMap.keys)
//        if (removekeys.isNotEmpty()){
//            removekeys.forEach { oddsDetailMap.remove(it) }
//        }
//        newOddsMap.forEach { oddsDetailMap[] = it.value.odds }
//        return oddsDetailMap
//    }
//    fun Odds.updateOdds(){
//
//    }

    /**
     * 加入新增的玩法並同時更新已有玩法的資料再以rowSort排序
     */
    @Synchronized
    fun updateMatchOddsMap(
        oddsDetailDataList: ArrayList<OddsDetailListData>,
        matchOddsChangeEvent: MatchOddsChangeEvent,
        playCate: org.cxct.sportlottery.network.myfavorite.PlayCate?
    ): ArrayList<OddsDetailListData>? {
        var newOddsMap = matchOddsChangeEvent.odds ?: return null

        //若有新玩法的話需要重新setData
        var addedNewOdds = false
        //若有舊玩法被移除的話
        var removedOldOdds = false

        var updateOldOdds = false

        val newOddsDetailDataList: ArrayList<OddsDetailListData> = ArrayList()
        if (oddsDetailDataList.isNotEmpty()) {
            newOddsDetailDataList.addAll(oddsDetailDataList)
        }

        newOddsMap = newOddsMap.replaceNameMap(oddsDetailDataList.firstOrNull()?.matchInfo)
        matchOddsChangeEvent.odds = newOddsMap
        val oldDetailDataList = oddsDetailDataList.iterator()
        while (oldDetailDataList.hasNext()) {
            val oldListData = oldDetailDataList.next()
            val newOddsData = newOddsMap[oldListData.gameType]
            if (newOddsData == null) {
                if (matchOddsChangeEvent.isReplaceAll()) {
                    newOddsDetailDataList.remove(oldListData)
                }
            } else {
                val dataOddsList = oldListData.oddArrayList
                val socketOddsList = newOddsData.odds

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
                        dataOddsList.add(newOdd)
                        addedNewOdds = true
                    }
                }
            }
        }

//        //有新賠率盤口
//        matchOddsChangeEvent.odds?.forEach { (key, value) ->
//            oddsDetailDataList.filter { it.gameType == key }.forEach {
//                val dataOddsList = it.oddArrayList
//                val socketOddsList = value.odds
//
//                //賠率id list
//                val dataGroupByList = dataOddsList.map { odd -> odd?.id }
//                val socketGroupByList = socketOddsList?.map { odd -> odd?.id } ?: listOf()
//
//                //新的Odd
//                val newOddsId = socketGroupByList.filter { socketId ->
//                    !dataGroupByList.contains(socketId)
//                }
//
//                newOddsId.forEach { newOddId ->
//                    socketOddsList?.find { socketOdd ->
//                        socketOdd?.let { socketOddNotNull ->
//                            socketOddNotNull.id == newOddId
//                        } ?: false
//                    }?.let { newOdd ->
//                        it.oddArrayList.add(newOdd)
//                        addedNewOdds = true
//                    }
//                }
//            }
//        }


        matchOddsChangeEvent.odds?.filter { socketOddsMap ->
            socketOddsMap.value.odds?.all { it?.status == 2 } ?: false
        }?.forEach { lostOddsMap ->
            val needRemoveOddsList =
                newOddsDetailDataList.find { oddsMap -> oddsMap.gameType == lostOddsMap.key }
            if (needRemoveOddsList != null) {
                newOddsDetailDataList.remove(needRemoveOddsList)
                removedOldOdds = true
            }
        }

        /**
         * 若有移除玩法的話, 重新配置原index
         * @see OddsDetailListData.originPosition
         */
        if (removedOldOdds) {
            newOddsDetailDataList.forEachIndexed { index, oddsDetailListData ->
                oddsDetailListData.originPosition = index
            }
        }
        //新玩法
        val newPlay = matchOddsChangeEvent.odds?.filter { socketOdds ->
            oddsDetailDataList.find { it.gameType == socketOdds.key } == null
                    && socketOdds.value.odds?.all { it?.status != BetStatus.DEACTIVATED.code } ?: true //新玩法應過濾 DEACTIVATED odds
        }
        //加入新玩法
        newPlay?.forEach { (key, value) ->
            val filteredOddList = mutableListOf<Odd?>()
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
                value.rowSort, matchInfo = oddsDetailDataList.firstOrNull()?.matchInfo
            ).apply {
                originPosition = newOddsDetailDataList.size
            })
            addedNewOdds = true
        }
        newOddsDetailDataList.apply {
            forEach { oddsDetailListData ->
                    if(updateMatchOdds(oddsDetailListData, matchOddsChangeEvent)){
                        updateOldOdds = true
                    }
                oddsDetailListData.oddArrayList.sortWith(compareBy({ it?.marketSort }, { it?.rowSort }))
            }
            setupPinList(playCate)
        }
        return if (addedNewOdds || removedOldOdds || updateOldOdds) newOddsDetailDataList else null
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

        this.forEach { it.isPin = false }

        pinList.forEach { pinOddsDetailData ->
            pinOddsDetailData.isPin = true

            add(epsSize, this.removeAt(this.indexOf(pinOddsDetailData)))
        }
    }


    fun updateOddStatus(matchOdd: MatchOdd, globalStopEvent: FrontWsEvent.GlobalStopEvent): Boolean {
        var isNeedRefresh = false
        val producerId = globalStopEvent.producerId?.value
        val noneProducerId = producerId == null

        matchOdd.oddsMap?.values?.forEach { odds ->
            odds?.forEach { odd ->
                if ((noneProducerId || producerId == odd.producerId) && odd.status != BetStatus.LOCKED.code) {
                    odd.status = BetStatus.LOCKED.code
                    isNeedRefresh = true
                }
            }
        }

        matchOdd.oddsEps?.eps?.forEach { odd ->
            if ((noneProducerId || producerId == odd.producerId) && odd.status != BetStatus.LOCKED.code) {
                odd.status = BetStatus.LOCKED.code
                isNeedRefresh = true
            }
        }

//        if (isNeedRefresh) {
//            matchOdd.updateOddStatus()
//        }

        return isNeedRefresh
    }

    fun updateOddStatus(
        oddsDetailListData: OddsDetailListData, globalStopEvent: FrontWsEvent.GlobalStopEvent
    ): Boolean {
        var isNeedRefresh = false
        val producerId = globalStopEvent.producerId?.value
        val oddArrayList = if (producerId == null) {
            oddsDetailListData.oddArrayList
        } else {
            oddsDetailListData.oddArrayList.filter { odd -> producerId == odd?.producerId }.toMutableList()
        }

        oddArrayList.forEach { odd ->
            if (odd?.status != BetStatus.LOCKED.code) {
                odd?.status = BetStatus.LOCKED.code
                isNeedRefresh = true
            }
        }


//        if (isNeedRefresh) {
//            oddsDetailListData.updateOddStatus()
//        }

        return isNeedRefresh
    }

    //處理收到 matchOddsLock event 的狀態變化
    fun updateOddStatus(
        oddsDetailListData: OddsDetailListData
    ): Boolean {
        var isNeedRefresh = false

        oddsDetailListData.oddArrayList.forEach { odd ->
            odd?.status = BetStatus.LOCKED.code
            isNeedRefresh = true
        }

        return isNeedRefresh
    }

    fun updateOddStatus(
        betInfoListData: BetInfoListData, matchStatusEvent: FrontWsEvent.MatchStatusChangeEvent
    ): Boolean {
        var isNeedRefresh = false

        if (betInfoListData.matchOdd.matchId == matchStatusEvent.matchStatusCO?.matchId) {
            betInfoListData.matchOdd.status = BetStatus.LOCKED.code
            betInfoListData.amountError = true
            isNeedRefresh = true
        }

        return isNeedRefresh
    }

    fun updateOddStatus(matchOdd: MatchOdd, matchOddsLockEvent: FrontWsEvent.MatchOddsLockEvent): Boolean {
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
                isNeedRefresh = true
            }
        }

        return isNeedRefresh
    }



    private fun insertMatchOdds(
        oddsDetailListData: OddsDetailListData, matchOddsChangeEvent: MatchOddsChangeEvent
    ): Boolean {
        val odds = matchOddsChangeEvent.odds?.get(matchOddsChangeEvent.odds?.keys?.find {
            it == oddsDetailListData.gameType
        })

        oddsDetailListData.oddArrayList = odds?.odds ?: mutableListOf()

        return odds?.odds?.isNotEmpty() ?: false
    }

    fun insertPlayCate(
        matchOdd: MatchOdd, oddsChangeEvent: OddsChangeEvent, matchType: MatchType?
    ): Boolean {
        /**
         * MatchType為波膽時, 快捷玩法只需出現反波膽, 其餘MatchType的快捷玩法不得出現反波膽
         */
        val socketQuickPlayCateList = when (matchType) {
            MatchType.CS -> {
                oddsChangeEvent.quickPlayCateList?.filter { it.code == QuickPlayCate.QUICK_LCS.value }
            }

            else -> {
                oddsChangeEvent.quickPlayCateList?.filter { it.code != QuickPlayCate.QUICK_LCS.value }
            }
        }
        if (matchOdd.quickPlayCateList == null) {
            matchOdd.quickPlayCateList = socketQuickPlayCateList?.toMutableList()
        } else {
            matchOdd.quickPlayCateList?.addAll(socketQuickPlayCateList ?: listOf())
        }
        return oddsChangeEvent.quickPlayCateList?.isNotEmpty() ?: false
    }

    /**
     * 置換玩法翻譯名稱
     * 配置{H}, {C}翻譯取代文字
     * 新增{E} -> 附加訊息(extInfo)
     */
    private fun Map<String, Odds>.replaceNameMap(matchInfo: org.cxct.sportlottery.network.odds.MatchInfo?): Map<String, Odds> {
        val newMap = this
        newMap.forEach { (playCateCode, value) ->
            value.nameMap?.toMap()?.forEach { (playCode, translateName) ->

//                value.extInfoReplaced = translateName?.contains("{E}") == true

                val newNameMap = this?.get(playCateCode)?.nameMap?.toMutableMap()
                val replacedName =
                    translateName?.replace("||", "\n")?.replace(
                        "{S}",
                        (if (playCateCode.contains(":")) playCateCode.split(":")
                            .getOrNull(1) else value.odds?.firstOrNull()?.extInfo) ?: "{S}"
                    )?.replace("{H}", matchInfo?.homeName ?: "{H}")?.replace("{C}", matchInfo?.awayName ?: "{C}")?.replace(
                        "{E}",
                        value.odds?.maxByOrNull { it?.extInfo?.toBigDecimalOrNull() ?: BigDecimal.ZERO }?.extInfo
                            ?: matchInfo?.extInfo ?: "{E}"
                    )

                newNameMap?.put(playCode, replacedName)

                newMap[playCateCode]?.nameMap = newNameMap
            }

            value.odds.replaceNameMap(matchInfo)
        }

        return newMap.toMap()
    }

    /**
     * 置換盤口訊息翻譯名稱
     * 配置{H}, {C}, {S}翻譯取代文字
     * 新增{E} -> 附加訊息(extInfo)
     * {P} -> spread
     * 需在replaceScore已經配置之後執行此method才會替換{S}
     */
    private fun MutableList<Odd?>?.replaceNameMap(matchInfo: org.cxct.sportlottery.network.odds.MatchInfo?) {
        this?.toList()?.forEach { odd ->
            odd?.nameMap?.toMap()?.forEach { (playCode, translateName) ->

                val newNameMap = odd.nameMap?.toMutableMap()
                val replacedName = translateName?.replace("||", "\n")?.replace("{S}", odd.replaceScore ?: "{S}")
                    ?.replace("{H}", matchInfo?.homeName ?: "{H}")?.replace("{C}", matchInfo?.awayName ?: "{C}")
                    ?.replace("{E}", matchInfo?.extInfo ?: "{E}")?.replace("{P}", odd.spread ?: "{P}")

                newNameMap?.put(playCode, replacedName)

                odd.nameMap = newNameMap

            }
        }
    }


    private fun refreshMatchOdds(
        oddsDetailListData: OddsDetailListData, matchOddsChangeEvent: MatchOddsChangeEvent
    ): Boolean {
        var isNeedRefresh = false

        val odds = matchOddsChangeEvent.odds?.get(matchOddsChangeEvent.odds?.keys?.find {
            it == oddsDetailListData.gameType
        })

        //更新玩法翻譯
        odds?.nameMap?.let {
            oddsDetailListData.nameMap = it
        }

        oddsDetailListData.oddArrayList.forEach { odd ->
            val oddSocket = odds?.odds?.find { oddSocket ->
                oddSocket?.id == odd?.id
            }
            if (isNeedUpdateOdd(oddSocket,odd)){
            oddSocket?.let {
                odd?.odds?.let { oddValue ->
                    oddSocket.odds?.let { oddSocketValue ->
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
                if (odd?.marketSort != oddSocket.marketSort) {
                    odd?.marketSort = oddSocket.marketSort
                    isNeedRefresh = true
                }
                if (odd?.rowSort != oddSocket.rowSort) {
                    odd?.rowSort = oddSocket.rowSort
                    isNeedRefresh = true
                }
                if (odd?.version != oddSocket.version) {
                    odd?.version = oddSocket.version
                    isNeedRefresh = true
                }
                if (oddsDetailListData.rowSort != odds.rowSort) {
                    oddsDetailListData.rowSort = odds.rowSort
                    isNeedRefresh = true
                }
            }
            }
        }
        return isNeedRefresh
    }

    fun refreshPlayCate(
        matchOdd: MatchOdd, oddsChangeEvent: OddsChangeEvent, matchType: MatchType?
    ): Boolean {
        var isNeedRefresh = false

        /**
         * MatchType為波膽時, 快捷玩法只需出現反波膽, 其餘MatchType的快捷玩法不得出現反波膽
         */
        val socketQuickPlayCateList = when (matchType) {
            MatchType.CS -> {
                oddsChangeEvent.quickPlayCateList?.filter { it.code == QuickPlayCate.QUICK_LCS.value }
            }

            else -> {
                oddsChangeEvent.quickPlayCateList?.filter { it.code != QuickPlayCate.QUICK_LCS.value }
            }
        }

        socketQuickPlayCateList?.forEach { quickPlayCateSocket ->
            when (matchOdd.quickPlayCateList?.map { it.code }?.contains(quickPlayCateSocket.code)) {
                false -> {
                    matchOdd.quickPlayCateList?.add(quickPlayCateSocket)
                    isNeedRefresh = true
                }
            }
        }
        return isNeedRefresh
    }

    private fun Map<String, List<Odd>?>.toMutableFormat(): MutableMap<String, MutableList<Odd>?> {
        return this.mapValues { map ->
            map.value?.toMutableList() ?: mutableListOf()
        }.toMutableMap()
    }

    fun Map<String, List<Odd?>?>.toMutableFormat_1(): MutableMap<String, MutableList<Odd?>?> {
        return this.mapValues { map ->
            map.value?.toMutableList() ?: mutableListOf()
        }.toMutableMap()
    }

    fun MatchOdd.updateOddStatus() {
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
    /**
     * 判断是否需要更新本地赔率
     * 新消息过来的version>旧的version才替换更新，为了保持兼容性为空或为0也更新，涉及ODDS_CHANGE，MATCH_ODDS_CHANGE
     */
    fun isNeedUpdateOdd(socketOdd: Odd?,oldOdd: Odd?) :Boolean{
        if (socketOdd==null) return false
        if (oldOdd==null) return true
        return socketOdd?.version > oldOdd.version || socketOdd.version==0
    }
}