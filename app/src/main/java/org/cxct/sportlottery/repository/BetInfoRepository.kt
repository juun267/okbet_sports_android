package org.cxct.sportlottery.repository


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.enum.SpreadState
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.index.playquotacom.t.PlayQuota
import org.cxct.sportlottery.network.index.playquotacom.t.PlayQuotaComData
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.parlaylimit.ParlayBetLimit
import org.cxct.sportlottery.util.parlaylimit.ParlayLimitUtil
import kotlin.math.abs


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

    private val _betIDList = MutableLiveData<Event<MutableList<String>>>().apply {
        value = Event(mutableListOf())
    }
    val betIDList: LiveData<Event<MutableList<String>>>
        get() = _betIDList

    private val _showBetUpperLimit = MutableLiveData<Event<Boolean>>()
    val showBetUpperLimit: LiveData<Event<Boolean>>
        get() = _showBetUpperLimit

    private val _matchOddList = MutableLiveData<MutableList<MatchOdd>>()
    val matchOddList: LiveData<MutableList<MatchOdd>>
        get() = _matchOddList


    private val _parlayList = MutableLiveData<MutableList<ParlayOdd>>()
    val parlayList: LiveData<MutableList<ParlayOdd>>
        get() = _parlayList

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
                updatePlayQuota()
            }
        }
    var oddsType: OddsType = OddsType.EU
        set(value) {
            if (value != field) {
                field = value
            }
        }

    //用於投注單頁面顯示提醒介面
    val showOddsChangeWarn: LiveData<Boolean>
        get() = _showOddsChangeWarn
    private val _showOddsChangeWarn = MutableLiveData<Boolean>()

    val showOddsCloseWarn: LiveData<Boolean>
        get() = _showOddsCloseWarn
    private val _showOddsCloseWarn = MutableLiveData<Boolean>()

    //有投注額的單注封盤
    val hasBetPlatClose: LiveData<Boolean>
        get() = _hasBetPlatClose
    private val _hasBetPlatClose = MutableLiveData<Boolean>()

    private val gameFastBetOpenedSharedPreferences by lazy {
        androidContext.getSharedPreferences(
            GameViewModel.GameFastBetOpenedSP,
            Context.MODE_PRIVATE
        )
    }

    @Deprecated("串關邏輯修改,使用addInBetOrderParlay")
    fun addInBetInfoParlay() {
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()

        if (betList.size == 0) {
            return
        }

        val gameType = GameType.getGameType(betList.getOrNull(0)?.matchOdd?.gameType)

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
    private fun updateBetOrderParlay(betList: MutableList<BetInfoListData>) {
        if (betList.size == 0) {
            return
        }

        var hasPointMark = false //若有被標記就進行串關組合了
        //先檢查有沒有冠軍類別, 若有則全部紅色標記
        val hasMatchType = betList.find { it.matchType == MatchType.OUTRIGHT } != null

        //檢查有沒有反波膽
        val hasLcsGameType = betList.find { it.matchOdd.playCode == PlayCate.LCS.name } != null

        //檢查是否有不同的球賽種類
        val gameType = GameType.getGameType(betList.getOrNull(0)?.matchOdd?.gameType)

        //檢查是否有相同賽事
        val matchIdList: MutableMap<String, MutableList<Int>> = mutableMapOf()
        betList.forEachIndexed { index, betInfoListData ->
            matchIdList[betInfoListData.matchOdd.matchId]?.add(index)
                ?: run { matchIdList[betInfoListData.matchOdd.matchId] = mutableListOf(index) }
        }

        betList.forEach {
            if (hasLcsGameType || hasMatchType || gameType != GameType.getGameType(it.matchOdd.gameType) || matchIdList[it.matchOdd.matchId]?.size ?: 0 > 1) {
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

            _matchOddList.postValue(parlayMatchOddList)

            if (!hasPointMark) {
                val newParlayList = updateParlayOddOrder(
                    getParlayOdd(MatchType.PARLAY, it, parlayMatchOddList, true).toMutableList()
                )
                if (!_parlayList.value.isNullOrEmpty() && _parlayList.value?.size == newParlayList.size) {
                    _parlayList.value?.forEachIndexed { index, parlayOdd ->
                        newParlayList[index].apply {
                            betAmount = parlayOdd.betAmount
                            inputBetAmountStr = parlayOdd.inputBetAmountStr
                            allSingleInput = parlayOdd.allSingleInput
                            amountError = parlayOdd.amountError
                        }
                    }
                }
                _parlayList.value = newParlayList
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

    fun removeItem(oddId: String?) {
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()

        val item = betList.find { it.matchOdd.oddsId == oddId }
        betList.remove(item)

        updateQuickListManager(betList)

        val oddIDStr = oddId ?: ""
        val oddIDArray = _betIDList.value?.peekContent() ?: mutableListOf()
        oddIDArray.remove(oddIDStr)
        _betIDList.postValue(Event(oddIDArray))

        _removeItem.postValue(Event(item?.matchOdd?.matchId))
        updateBetOrderParlay(betList)
        checkBetInfoContent(betList)
        _betInfoList.postValue(Event(betList))
    }

    fun removeClosedPlatItem() {
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()

        val oddIDArray = _betIDList.value?.peekContent() ?: mutableListOf()

        val needRemoveList =
            betList.filter { it.matchOdd.status == BetStatus.LOCKED.code || it.matchOdd.status == BetStatus.DEACTIVATED.code }
        needRemoveList.forEach {
            betList.remove(it)
            oddIDArray.remove(it.matchOdd.oddsId)
            _removeItem.value = Event(it.matchOdd.matchId)
        }

        updateQuickListManager(betList)

        updateBetOrderParlay(betList)
        checkBetInfoContent(betList)
        _betIDList.postValue(Event(oddIDArray))
        _betInfoList.postValue(Event(betList))
    }


    fun clear() {
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()
        val oddIDArray = _betIDList.value?.peekContent() ?: mutableListOf()
        betList.clear()
        oddIDArray.clear()
        _matchOddList.value?.clear()
        _parlayList.value?.clear()

        updateQuickListManager(betList)

        checkBetInfoContent(betList)
        _betIDList.postValue(Event(oddIDArray))
        _betInfoList.postValue(Event(betList))
    }


    fun addInBetInfo() {
        _showBetInfoSingle.postValue(Event(false))
    }

    /**
     * 點擊賠率按鈕加入投注清單, 並產生串關注單
     */
    fun addInBetInfo(
        matchType: MatchType,
        gameType: GameType,
        playCateCode: String,
        playCateName: String,
        playName: String,
        matchInfo: MatchInfo,
        odd: Odd,
        subscribeChannelType: ChannelType,
        playCateMenuCode: String? = null,
        oddsType: OddsType?,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?
    ) {
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()
        oddsType?.let {
            this.oddsType = it
        }
        if (betList.size >= BET_INFO_MAX_COUNT) {
            _showBetUpperLimit.postValue(Event(true))
            return
        }

        val emptyFilter = { item: String? ->
            if (item.isNullOrEmpty()) null else item
        }

        val betInfoMatchOdd = MatchOddUtil.transfer(
            matchType = matchType,
            gameType = gameType.key,
            playCateCode = playCateCode,
            playCateName = playCateName,
            playName = playName,
            matchInfo = matchInfo,
            odd = odd
        )

        betInfoMatchOdd?.let {
            val data = BetInfoListData(
                betInfoMatchOdd,
                getParlayOdd(matchType, gameType, mutableListOf(it)).first(),
                betPlayCateNameMap
            ).apply {
                this.matchType = matchType
                this.subscribeChannelType = subscribeChannelType
                this.playCateMenuCode = playCateMenuCode
                this.outrightMatchInfo = matchInfo
            }

            val oddIDArray = _betIDList.value?.peekContent() ?: mutableListOf()
            oddIDArray.add(it.oddsId)
            _betIDList.postValue(Event(oddIDArray))

            betList.add(data)

            updateQuickListManager(betList)

            //產生串關注單
            updateBetOrderParlay(betList)
            checkBetInfoContent(betList)
            _betInfoList.postValue(Event(betList))
            if (betList.size == 1 && matchType != MatchType.PARLAY) {
                _showBetInfoSingle.postValue(Event(true))
            }
        }
    }

    /**
     * @param isParlayBet 2021/10/29新增, gameType為GameType.PARLAY時不代表該投注為串關投注, 僅由組合後產生的投注才是PARLAY
     */
    fun getParlayOdd(
        matchType: MatchType,
        gameType: GameType,
        matchOddList: MutableList<MatchOdd>,
        isParlayBet: Boolean = false
    ): List<ParlayOdd> {

        val playQuota: PlayQuota? = when {
            matchType == MatchType.OUTRIGHT -> {
                when (gameType) {
                    GameType.FT -> playQuotaComData?.oUTRIGHTFT
                    GameType.BK -> playQuotaComData?.oUTRIGHTBK
                    GameType.TN -> playQuotaComData?.oUTRIGHTTN
                    GameType.VB -> playQuotaComData?.oUTRIGHTVB
                    GameType.BM -> playQuotaComData?.oUTRIGHTBM
                    GameType.TT -> playQuotaComData?.oUTRIGHTTT
                    GameType.IH -> playQuotaComData?.oUTRIGHTIH
                    GameType.BX -> playQuotaComData?.oUTRIGHTBX
                    GameType.CB -> playQuotaComData?.oUTRIGHTCB
                    GameType.CK -> playQuotaComData?.oUTRIGHTCK
                    GameType.BB -> playQuotaComData?.oUTRIGHTBB
                    GameType.RB -> playQuotaComData?.oUTRIGHTRB
                    GameType.AFT -> playQuotaComData?.oUTRIGHTAFT
                    GameType.MR -> playQuotaComData?.oUTRIGHTMR
                    GameType.GF -> playQuotaComData?.oUTRIGHTGF
                    else -> playQuotaComData?.oUTRIGHTFT //測試用，需再添加各項球類playQuotaComData
                }
            }

            isParlayBet -> {
                when (gameType) {
                    GameType.FT -> playQuotaComData?.pARLAYFT
                    GameType.BK -> playQuotaComData?.pARLAYBK
                    GameType.TN -> playQuotaComData?.pARLAYTN
                    GameType.VB -> playQuotaComData?.pARLAYVB
                    GameType.BM -> playQuotaComData?.pARLAYBM
                    GameType.TT -> playQuotaComData?.pARLAYTT
                    GameType.IH -> playQuotaComData?.pARLAYIH
                    GameType.BX -> playQuotaComData?.pARLAYBX
                    GameType.CB -> playQuotaComData?.pARLAYCB
                    GameType.CK -> playQuotaComData?.pARLAYCK
                    GameType.BB -> playQuotaComData?.pARLAYBB
                    GameType.RB -> playQuotaComData?.pARLAYRB
                    GameType.AFT -> playQuotaComData?.pARLAYAFT
                    GameType.MR -> playQuotaComData?.pARLAYMR
                    GameType.GF -> playQuotaComData?.pARLAYGF
                    else -> playQuotaComData?.oUTRIGHTFT //測試用，需再添加各項球類playQuotaComData
                }
            }
            else -> {
                when (gameType) {
                    GameType.FT -> playQuotaComData?.sINGLEFT
                    GameType.BK -> playQuotaComData?.sINGLEBK
                    GameType.TN -> playQuotaComData?.sINGLETN
                    GameType.VB -> playQuotaComData?.sINGLEVB
                    GameType.BM -> playQuotaComData?.sINGLEBM
                    GameType.TT -> playQuotaComData?.sINGLETT
                    GameType.IH -> playQuotaComData?.sINGLEIH
                    GameType.BX -> playQuotaComData?.sINGLEBX
                    GameType.CB -> playQuotaComData?.sINGLECB
                    GameType.CK -> playQuotaComData?.sINGLECK
                    GameType.BB -> playQuotaComData?.sINGLEBB
                    GameType.RB -> playQuotaComData?.sINGLERB
                    GameType.AFT -> playQuotaComData?.sINGLEAFT
                    GameType.MR -> playQuotaComData?.sINGLEMR
                    GameType.GF -> playQuotaComData?.sINGLEGF
                    else -> playQuotaComData?.oUTRIGHTFT //測試用，需再添加各項球類playQuotaComData
                }
            }
        }

        val oddsList = matchOddList.map {
            Pair(it.odds.toBigDecimal(), it.isOnlyEUType)
        }

        val oddsIndexList = oddsList.map {
            oddsList.indexOf(it)
        }

        val parlayComList = ParlayLimitUtil.getCom(oddsIndexList.toIntArray())

        val parlayBetLimitMap = ParlayLimitUtil.getParlayLimit(
            oddsList,
            parlayComList,
            playQuota?.max?.toBigDecimal(),
            playQuota?.min?.toBigDecimal()
        )
        var parlayBetLimit = 9999
        var userSelfLimit = MultiLanguagesApplication.getInstance()?.userInfo?.value?.perBetLimit

        return parlayBetLimitMap.map {
            parlayBetLimit = it.value.max.toInt()
            var maxBet = 9999
            val maxBetMoney = GameConfigManager.maxBetMoney ?: 9999999
            val maxCpBetMoney = GameConfigManager.maxCpBetMoney
            val maxParlayBetMoney = GameConfigManager.maxParlayBetMoney ?: 9999999
            val maxPayout = MultiLanguagesApplication.getInstance()?.userInfo?.value?.maxPayout ?: 9999999.0
            if(it.value.num > 1){
                //大於1 即為組合型串關 最大下注金額有特殊規則
                maxBet = calculateComboMaxBet(it.value,playQuota?.max)
            }else{
                //根據賽事類型的投注上限
                val matchTypeMaxBetMoney = when {
                    matchType == MatchType.PARLAY && isParlayBet -> maxParlayBetMoney
                    matchType == MatchType.OUTRIGHT -> maxCpBetMoney
                    else -> maxBetMoney
                } ?: 0

                //風控投注額上限
                val hdOddsPayout =
                    maxPayout.div(if (it.value.isOnlyEUType) it.value.odds.toDouble() - 1 else it.value.hdOdds.toDouble())
                        .toInt()

                //region parlayBetLimit(球類賽事類型投注額上限), matchTypeMaxBetMoney(會員層級賽事類型投注額上限), userSelfLimit(自我禁制投注額上限), hdOddsPayout(風控投注額上限) 取最小值作為投注額上限
                listOf(
                    parlayBetLimit,
                    matchTypeMaxBetMoney,
                    userSelfLimit ?: 0,
                    hdOddsPayout
                ).filter { limit -> limit > 0 }.minOrNull()?.let { minLimit ->
                    maxBet = minLimit
                }
                //endregion

                /*when (matchType) {
                    MatchType.PARLAY -> {
                        if (isParlayBet) {
                        //TODO 向Martin確認 此處的 maxBetMoney 是否錯誤 應為 maxParlayBetMoney
                            maxBet =
                                if (maxParlayBetMoney < parlayBetLimit) maxBetMoney else parlayBetLimit
                        } else {
                            maxBet = if (maxBetMoney < parlayBetLimit) maxBetMoney else parlayBetLimit
                        }
                    }
                    MatchType.OUTRIGHT -> maxBet =
                        maxCpBetMoney?.let { if (maxCpBetMoney < parlayBetLimit) maxCpBetMoney else parlayBetLimit }
                            ?: parlayBetLimit

                    else -> maxBet =
                        maxBetMoney.let { if (maxBetMoney < parlayBetLimit) maxBetMoney else parlayBetLimit }
                            ?: parlayBetLimit
                }*/

                //[Martin]為馬來盤＆印度計算投注上限
                if (oddsType == OddsType.MYS && !it.value.isOnlyEUType) {
                    if ((matchOddList.getOrNull(0)?.malayOdds ?: 0.0) < 0.0 && oddsList.size <= 1) {
                        val malayMax = ((playQuota?.max?.toDouble()?.plus(abs(matchOddList.getOrNull(0)?.malayOdds ?: 0.0)))?.toInt())?.minus(1)?: 0
                        val malayUserMax = (maxBetMoney.div(abs(matchOddList.getOrNull(0)?.malayOdds ?: 0.0))).toInt()
                        listOf(
                            malayMax,
                            malayUserMax,
                            parlayBetLimit,
                            maxPayout.toInt(),
                            userSelfLimit ?: 0
                        ).filter { limit -> limit > 0 }.minOrNull()?.let { minLimit ->
                            maxBet = minLimit
                        }
                    }
                } else if (oddsType == OddsType.IDN && !it.value.isOnlyEUType) {
                    if (matchOddList.getOrNull(0)?.indoOdds ?: 0.0 < 0.0 && oddsList.size <= 1) {
                        //印度賠付額上限
                        val indoMax = ((playQuota?.max?.toDouble()?.plus(abs(matchOddList.getOrNull(0)?.indoOdds ?: 0.0)))?.toInt())?.minus(1)?: 0
                        //印度使用者投注上限
                        val indoUserMax = maxBetMoney.div(abs(matchOddList.getOrNull(0)?.indoOdds ?: 0.0)).toInt()

                        //region indoMax(印度賠付額上限), indoUserMax(印度使用者投注上限), hdOddsPayout(風控投注額上限), userSelfLimit(自我禁制投注額上限) 取最小值作為投注額上限
                        listOf(
                            indoMax,
                            indoUserMax,
                            maxPayout.toInt(),
                            userSelfLimit ?: 0
                        ).filter { limit -> limit > 0 }.minOrNull()?.let { minLimit ->
                            maxBet = minLimit
                        }
                        //endregion
                    }
                }
                //TODO 問Martin, 這一段的用意
                /*else if(oddsType == OddsType.IDN) {
                        if (matchOddList[0].indoOdds < 0 && oddsList.size <= 1) {
                            val indoMax = (((playQuota?.max?.toDouble()?.plus(abs(matchOddList[0].indoOdds))) ?: 0).toInt()) - 1
                            maxBet = if (maxBetMoney < indoMax) maxBetMoney else indoMax
                        }
                    }*/
            }

            ParlayOdd(
                parlayType = it.key,
                max = maxBet,
                min = it.value.min.toInt(),
                num = it.value.num,
                odds = it.value.odds.toDouble(),
                hkOdds = it.value.hdOdds.toDouble(),
                //Martin
                malayOdds = if (oddsList.size > 1) it.value.odds.toDouble() else matchOddList.getOrNull(0)?.malayOdds?:0.0,
                indoOdds = if (oddsList.size > 1) it.value.odds.toDouble() else matchOddList.getOrNull(0)?.indoOdds?:0.0
            )
        }
    }

    private fun calculateComboMaxBet(
        parlayBetLimit: ParlayBetLimit,
        max: Int?,
    ): Int {
        var tempMax = max?.times(parlayBetLimit.num)
        return tempMax?.div(parlayBetLimit.hdOdds.toDouble())!!.toInt()
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

        updateBetInfoList.forEach { betInfoListData ->
            betInfoListData.matchType?.let { matchType ->
                val gameType = GameType.getGameType(betInfoListData.matchOdd.gameType)
                gameType?.let {
                    betInfoListData.parlayOdds = getParlayOdd(
                        matchType,
                        gameType,
                        mutableListOf(betInfoListData.matchOdd)
                    ).first()
                }
            }
        }

        updateQuickListManager(updateBetInfoList)

        checkBetInfoContent(updateBetInfoList)
        updateBetOrderParlay(updateBetInfoList)
        _betInfoList.value = Event(updateBetInfoList)
    }


    private fun updatePlayQuota() {

        val updateBetInfoList = _betInfoList.value?.peekContent()

        if (updateBetInfoList.isNullOrEmpty()) return

        val newList = mutableListOf<BetInfoListData>()
        updateBetInfoList.forEach { betInfoListData ->
            betInfoListData.matchType?.let { matchType ->
                //TODO Dean : review
                val gameType = GameType.getGameType(betInfoListData.matchOdd.gameType)
                gameType?.let {
                    val newBetInfoListData = BetInfoListData(
                        betInfoListData.matchOdd.copy(),
                        getParlayOdd(
                            matchType,
                            gameType,
                            mutableListOf(betInfoListData.matchOdd)
                        ).first(),
                        betInfoListData.betPlayCateNameMap
                    )
                    newBetInfoListData.matchType = betInfoListData.matchType
                    newBetInfoListData.input = betInfoListData.input
                    newBetInfoListData.betAmount = betInfoListData.betAmount
                    newBetInfoListData.pointMarked = betInfoListData.pointMarked
                    newList.add(newBetInfoListData)
                }
            }
        }

        updateQuickListManager(newList)

        checkBetInfoContent(newList)
        _betInfoList.value = Event(newList)
    }

    fun notifyBetInfoChanged(newList: MutableList<BetInfoListData>) {

        updateQuickListManager(newList)

        checkBetInfoContent(newList)
        _betInfoList.value = Event(newList)
    }


    /**
     * 檢查注單中賠率、盤口狀態
     */
    private fun checkBetInfoContent(betInfoList: MutableList<BetInfoListData>) {
        checkBetInfoOddChanged(betInfoList)
        checkBetInfoPlatStatus(betInfoList)
    }

    /**
     * 判斷是否有賠率變更
     */
    private fun checkBetInfoOddChanged(betInfoList: MutableList<BetInfoListData>) {
        var anyOddChanged = false
        betInfoList.forEach {
            if (it.matchOdd.oddsHasChanged) {
                anyOddChanged = true
                return@forEach
            }
        }
        _showOddsChangeWarn.postValue(anyOddChanged)
    }

    /**
     * 判斷是否有盤口關閉
     * 20210816, 有投注額的單注被封盤需要禁止投注
     */
    private fun checkBetInfoPlatStatus(betInfoList: MutableList<BetInfoListData>) {
        var hasPlatClose = false
        var hasBetPlatClose = false
        betInfoList.forEach {
            when (it.matchOdd.status) {
                BetStatus.LOCKED.code, BetStatus.DEACTIVATED.code -> {
                    if (it.betAmount > 0) {
                        hasPlatClose = true
                        hasBetPlatClose = true
                        return@forEach
                    }
                    hasPlatClose = true
                }
                else -> { //BetStatus.ACTIVATED.code
                    it.matchOdd.betAddError != null
                }
            }
        }
        _showOddsCloseWarn.postValue(hasPlatClose)
        _hasBetPlatClose.postValue(hasBetPlatClose)
    }

    fun setFastBetOpened(isOpen: Boolean) {
        val editor = gameFastBetOpenedSharedPreferences.edit()
        editor.putBoolean("isOpen", isOpen).apply()
    }

    fun getIsFastBetOpened(): Boolean{
        return gameFastBetOpenedSharedPreferences.getBoolean("isOpen", true)
    }

    fun updateBetAmount(input: String) {
        if ((_betInfoList.value?.peekContent()?.size ?: 0) == 0) return
        _betInfoList.value?.peekContent()?.first()?.inputBetAmountStr = input
    }

    private fun updateQuickListManager(betList: MutableList<BetInfoListData>) {
        //更新快捷投注項選中list
        QuickListManager.setQuickSelectedList(betList.map { bet -> bet.matchOdd.oddsId }.toMutableList())
    }

    fun updateMatchOdd(changeEvent: Any) {
        val newList: MutableList<Odd> = mutableListOf()
        when (changeEvent) {
            is OddsChangeEvent -> {
                changeEvent.odds.forEach { map ->
                    val value = map.value
                    value?.forEach { odd ->
                        odd?.let {
                            val newOdd = Odd(
                                extInfoMap = null,
                                id = odd.id,
                                name = null,
                                odds = odd.odds,
                                hkOdds = odd.hkOdds,
                                malayOdds = odd.malayOdds,
                                indoOdds = odd.indoOdds,
                                producerId = odd.producerId,
                                spread = odd.spread,
                                status = odd.status,
                            )
                            newList.add(newOdd)
                        }
                    }
                }
            }

            is MatchOddsChangeEvent -> {
                for ((_, value) in changeEvent.odds ?: mapOf()) {
                    value.odds?.forEach { odd ->
                        odd?.let { o ->
                            newList.add(o)
                        }
                    }
                }
            }
        }
        betInfoList.value?.peekContent()?.forEach {
            updateItem(it.matchOdd, newList)
        }
        notifyBetInfoChanged()

    }

    private fun updateItem(
        oldItem: MatchOdd,
        newList: List<Odd>
    ) {
        for (newItem in newList) {
            try {
                newItem.let {
                    if (it.id == oldItem.oddsId) {
                        //若賠率關閉則賠率不做高亮變化
                        newItem.status.let { status -> oldItem.status = status }

                        //賠率為啟用狀態時才去判斷是否有賠率變化
                        var currentOddsType = MultiLanguagesApplication.mInstance.mOddsType.value ?: OddsType.HK
                        if (it.odds == it.malayOdds) currentOddsType = OddsType.EU
                        if (oldItem.status == BetStatus.ACTIVATED.code) {
                            oldItem.oddState = getOddState(
                                getOdds(
                                    oldItem,
                                    currentOddsType
                                ), newItem
                            )

                            if (oldItem.oddState != OddState.SAME.state)
                                oldItem.oddsHasChanged = true
                        }

                        oldItem.spreadState = getSpreadState(oldItem.spread, it.spread ?: "")

                        if (oldItem.status == BetStatus.ACTIVATED.code) {
                            newItem.odds.let { odds -> oldItem.odds = odds ?: 0.0 }
                            newItem.hkOdds.let { hkOdds -> oldItem.hkOdds = hkOdds ?: 0.0 }
                            newItem.indoOdds.let { indoOdds -> oldItem.indoOdds = indoOdds ?: 0.0 }
                            newItem.malayOdds.let { malayOdds -> oldItem.malayOdds = malayOdds ?: 0.0 }
                            newItem.spread.let { spread -> oldItem.spread = spread ?: "" }
                        }

                        //從socket獲取後 賠率有變動並且投注狀態開啟時 需隱藏錯誤訊息
                        if (oldItem.oddState != OddState.SAME.state &&
                            oldItem.status == BetStatus.ACTIVATED.code
                        ) {
                            oldItem.betAddError = null
                        }

                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getOddState(
        oldItemOdds: Double,
        newOdd: Odd
    ): Int {
        //馬來盤、印尼盤為null時自行計算
        var newMalayOdds = 0.0
        var newIndoOdds = 0.0

        newOdd.hkOdds?.let {
            newMalayOdds =
                if (newOdd.hkOdds ?: 0.0 > 1) ArithUtil.oddIdfFormat(-1 / newOdd.hkOdds!!)
                    .toDouble() else newOdd.hkOdds ?: 0.0
            newIndoOdds =
                if (newOdd.hkOdds ?: 0.0 < 1) ArithUtil.oddIdfFormat(-1 / newOdd.hkOdds!!)
                    .toDouble() else newOdd.hkOdds ?: 0.0
        }

        val odds = when (MultiLanguagesApplication.mInstance.mOddsType.value) {
            OddsType.EU -> newOdd.odds
            OddsType.HK -> newOdd.hkOdds
            OddsType.MYS -> newOdd.malayOdds ?: newMalayOdds
            OddsType.IDN -> newOdd.indoOdds ?: newIndoOdds
            else -> null
        }
        val newOdds = odds ?: 0.0
        return when {
            newOdds == oldItemOdds -> OddState.SAME.state
            newOdds > oldItemOdds -> OddState.LARGER.state
            newOdds < oldItemOdds -> OddState.SMALLER.state
            else -> OddState.SAME.state
        }
    }

    private fun getSpreadState(oldSpread: String, newSpread: String): Int =
        when {
            newSpread != oldSpread -> SpreadState.DIFFERENT.state
            else -> SpreadState.SAME.state
        }
}