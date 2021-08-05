package org.cxct.sportlottery.repository


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.index.playquotacom.t.PlayQuota
import org.cxct.sportlottery.network.index.playquotacom.t.PlayQuotaComData
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.MatchOddUtil
import org.cxct.sportlottery.util.parlaylimit.ParlayLimitUtil


const val BET_INFO_MAX_COUNT = 10


class BetInfoRepository(val androidContext: Context) {


    private val _showBetInfoSingle = MutableLiveData<Event<Boolean?>>()


    val showBetInfoSingle: LiveData<Event<Boolean?>>
        get() = _showBetInfoSingle


    //每個畫面都要觀察
    private val _betInfoList = MutableLiveData<Event<MutableList<BetInfoListData>>>().apply {
        value = Event(mutableListOf())
    }
    val betInfoList: LiveData<Event<MutableList<BetInfoListData>>>
        get() = _betInfoList


    private val _matchOddList = MutableLiveData<MutableList<MatchOdd>>()
    val matchOddList: LiveData<MutableList<MatchOdd>>
        get() = _matchOddList


    private val _parlayList = MutableLiveData<MutableList<ParlayOdd>>()
    val parlayList: LiveData<MutableList<ParlayOdd>>
        get() = _parlayList


    val _isParlayPage = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isParlayPage: LiveData<Boolean>
        get() = _isParlayPage


    private val _removeItem = MutableLiveData<Event<String?>>()
    val removeItem: LiveData<Event<String?>>
        get() = _removeItem

    private val _betParlaySuccess = MutableLiveData(true)
    val betParlaySuccess: LiveData<Boolean>
        get() = _betParlaySuccess


    var playQuotaComData: PlayQuotaComData? = null
        set(value) {
            field = value
            field?.let {
                notifyBetInfoChanged()
            }
        }


    @Deprecated("串關邏輯修改,使用addInBetOrderParlay")
    fun addInBetInfoParlay() {
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()

        if (betList.size == 0) {
            return
        }

        val gameType = GameType.getGameType(betList[0].matchOdd.gameType)

        gameType?.let {
            val parlayMatchOddList = betList.map { betInfoListData ->
                betInfoListData.matchOdd
            }.toMutableList()

            _matchOddList.value = parlayMatchOddList

            _parlayList.value = updateParlayOddOrder(
                getParlayOdd(MatchType.PARLAY, it, parlayMatchOddList).toMutableList()
            )
        }
    }

    /**
     * 加入注單, 檢查串關邏輯, 無法串關的注單以紅點標記.
     */
    fun addInBetOrderParlay() {
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()

        if (betList.size == 0) {
            return
        }

        var hasPointMark = false //若有被標記就進行串關組合了
        //先檢查有沒有冠軍類別, 若有則全部紅色標記
        val hasMatchType = betList.find { it.matchType == MatchType.OUTRIGHT } != null

        //檢查是否有不同的球賽種類
        val gameType = GameType.getGameType(betList.getOrNull(0)?.matchOdd?.gameType)

        //檢查是否有相同賽事
        val matchIdList: MutableMap<String, MutableList<Int>> = mutableMapOf()
        betList.forEachIndexed { index, betInfoListData ->
            matchIdList[betInfoListData.matchOdd.matchId]?.add(index) ?: run { matchIdList[betInfoListData.matchOdd.matchId] = mutableListOf(index) }
        }

        betList.forEach {
            if (hasMatchType || gameType != GameType.getGameType(it.matchOdd.gameType) || matchIdList[it.matchOdd.matchId]?.size ?: 0 > 1) {
                hasPointMark = true
                it.pointMarked = true
            } else {
                it.pointMarked = false
            }
        }

        gameType?.let {
            val parlayMatchOddList = betList.map { betInfoListData ->
                betInfoListData.matchOdd
            }.toMutableList()

            _matchOddList.value = parlayMatchOddList

            if (!hasPointMark) {
                _parlayList.value = updateParlayOddOrder(
                    getParlayOdd(MatchType.PARLAY, it, parlayMatchOddList).toMutableList()
                )
                _betParlaySuccess.value = true
            } else {
                _parlayList.value = mutableListOf()
                _betParlaySuccess.value = false
            }
        }
    }


    private fun updateParlayOddOrder(parlayOddList: MutableList<ParlayOdd>): MutableList<ParlayOdd> {
        //將串起來的數量賠率移至第一項
        val pOdd = parlayOddList.find {
            matchOddList.value?.size.toString() + "C1" == it.parlayType
        }

        parlayOddList.remove(pOdd)

        pOdd?.let { po ->
            parlayOddList.add(0, po)
        }

        return parlayOddList
    }


    fun getCurrentBetInfoList() {
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()
        _betInfoList.postValue(Event(betList))
    }


    fun removeItem(oddId: String?) {
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()

        val item = betList.find { it.matchOdd.oddsId == oddId }
        betList.remove(item)
        _removeItem.postValue(Event(item?.matchOdd?.matchId))
        _betInfoList.postValue(Event(betList))
    }

    fun removeClosedPlatItem() {
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()
        val needRemoveList = betList.filter { it.matchOdd.status == BetStatus.LOCKED.code || it.matchOdd.status == BetStatus.DEACTIVATED.code }
        needRemoveList.forEach {
            betList.remove(it)
            _removeItem.value = Event(it.matchOdd.matchId)
        }

        _betInfoList.postValue(Event(betList))
    }


    fun clear() {
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()

        betList.clear()
        _matchOddList.value?.clear()
        _parlayList.value?.clear()

        _betInfoList.postValue(Event(betList))
    }


    fun addInBetInfo() {
        _showBetInfoSingle.postValue(Event(false))
    }


    fun addInBetInfo(
        matchType: MatchType,
        gameType: GameType,
        playCateName: String,
        playName: String,
        matchInfo: MatchInfo,
        odd: Odd
    ) {
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()

        if (betList.size >= BET_INFO_MAX_COUNT) return

        val emptyFilter = { item: String? ->
            if (item.isNullOrEmpty()) null else item
        }

        val betInfoMatchOdd = MatchOddUtil.transfer(
            matchType = matchType,
            gameType = gameType.key,
            playCateName = playCateName,
            playName = playName,
            matchInfo = matchInfo,
            odd = odd
        )

        betInfoMatchOdd?.let {
            val data = BetInfoListData(
                betInfoMatchOdd,
                getParlayOdd(matchType, gameType, mutableListOf(it)).first()
            ).apply {
                this.matchType = matchType
            }

            if (betList.size == 0) {
                _showBetInfoSingle.postValue(Event(true))
            }

            betList.add(data)
            _betInfoList.postValue(Event(betList))
        }
    }


    private fun getParlayOdd(
        matchType: MatchType,
        gameType: GameType,
        matchOddList: MutableList<MatchOdd>
    ): List<ParlayOdd> {

        val playQuota: PlayQuota? = when (matchType) {
            MatchType.OUTRIGHT -> {
                when (gameType) {
                    GameType.FT -> playQuotaComData?.oUTRIGHTFT
                    GameType.BK -> playQuotaComData?.oUTRIGHTBK
                    GameType.TN -> playQuotaComData?.oUTRIGHTTN
                    GameType.VB -> playQuotaComData?.oUTRIGHTVB
                }
            }

            MatchType.PARLAY -> {
                when (gameType) {
                    GameType.FT -> playQuotaComData?.pARLAYFT
                    GameType.BK -> playQuotaComData?.pARLAYBK
                    GameType.TN -> playQuotaComData?.pARLAYTN
                    GameType.VB -> playQuotaComData?.pARLAYVB
                }
            }
            else -> {
                when (gameType) {
                    GameType.FT -> playQuotaComData?.sINGLEFT
                    GameType.BK -> playQuotaComData?.sINGLEBK
                    GameType.TN -> playQuotaComData?.sINGLETN
                    GameType.VB -> playQuotaComData?.sINGLEVB
                }
            }
        }

        val oddsList = matchOddList.map {
            it.odds.toBigDecimal()
        }

        val oddsIndexList = oddsList.map {
            oddsList.indexOf(it)
        }

        val parlayComList = ParlayLimitUtil.getCom(androidContext, oddsIndexList.toIntArray())

        val parlayBetLimitMap = ParlayLimitUtil.getParlayLimit(
            oddsList,
            parlayComList,
            playQuota?.max?.toBigDecimal(),
            playQuota?.min?.toBigDecimal()
        )

        //串關規則Map
        val parlayRuleMap: MutableMap<String, String?> = mutableMapOf()
        for (parlayCom in parlayComList) {
            parlayRuleMap[parlayCom.parlayType] = parlayCom.rule
        }

        return parlayBetLimitMap.map {
            ParlayOdd(
                parlayType = it.key,
                max = it.value.max.toInt(),
                min = it.value.min.toInt(),
                num = it.value.num,
                odds = it.value.odds.toDouble(),
                hkOdds = it.value.hdOdds.toDouble(),
            ).apply {
                parlayRule = parlayRuleMap[it.key]
            }
        }
    }


    fun saveOddsHasChanged(matchOdd: MatchOdd) {
        val hasChanged = _betInfoList.value?.peekContent()?.find {
            it.matchOdd.oddsId == matchOdd.oddsId
        }
        hasChanged?.matchOdd?.oddsHasChanged = true
        hasChanged?.matchOdd?.oddState = OddState.SAME.state
    }

    fun notifyBetInfoChanged() {
        val updateBetInfoList = _betInfoList.value?.peekContent()

        if (updateBetInfoList.isNullOrEmpty()) return

        when (_isParlayPage.value) {
            true -> {
                val gameType = GameType.getGameType(updateBetInfoList[0].matchOdd.gameType)
                gameType?.let {
                    matchOddList.value?.let {
                        _parlayList.value =
                            getParlayOdd(MatchType.PARLAY, gameType, it).toMutableList()
                    }
                }
            }

            false -> {
                val newList = mutableListOf<BetInfoListData>()
                updateBetInfoList.forEach { betInfoListData ->
                    betInfoListData.matchType?.let { matchType ->
                        val gameType = GameType.getGameType(betInfoListData.matchOdd.gameType)
                        gameType?.let {
                            val newBetInfoListData = BetInfoListData(
                                betInfoListData.matchOdd,
                                getParlayOdd(
                                    matchType,
                                    gameType,
                                    mutableListOf(betInfoListData.matchOdd)
                                ).first()
                            )
                            newBetInfoListData.matchType = betInfoListData.matchType
                            newBetInfoListData.input = betInfoListData.input
                            newList.add(newBetInfoListData)
                        }
                    }
                }
                _betInfoList.value = Event(newList)
            }
        }
    }


}