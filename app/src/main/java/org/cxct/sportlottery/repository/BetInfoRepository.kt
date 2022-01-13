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
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.GameConfigManager
import org.cxct.sportlottery.util.MatchOddUtil
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
    private fun updateBetOrderParlay(betList: MutableList<BetInfoListData>) {
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
            matchIdList[betInfoListData.matchOdd.matchId]?.add(index)
                ?: run { matchIdList[betInfoListData.matchOdd.matchId] = mutableListOf(index) }
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
                val newParlayList = updateParlayOddOrder(
                    getParlayOdd(MatchType.PARLAY, it, parlayMatchOddList, true).toMutableList()
                )
                if (!_parlayList.value.isNullOrEmpty() && _parlayList.value?.size == newParlayList.size) {
                    _parlayList.value?.forEachIndexed { index, parlayOdd ->
                        newParlayList[index].apply {
                            betAmount = parlayOdd.betAmount
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
        _removeItem.postValue(Event(item?.matchOdd?.matchId))
        updateBetOrderParlay(betList)
        checkBetInfoContent(betList)
        _betInfoList.postValue(Event(betList))
    }

    fun removeClosedPlatItem() {
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()
        val needRemoveList =
            betList.filter { it.matchOdd.status == BetStatus.LOCKED.code || it.matchOdd.status == BetStatus.DEACTIVATED.code }
        needRemoveList.forEach {
            betList.remove(it)
            _removeItem.value = Event(it.matchOdd.matchId)
        }

        updateBetOrderParlay(betList)
        checkBetInfoContent(betList)
        _betInfoList.postValue(Event(betList))
    }


    fun clear() {
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()

        betList.clear()
        _matchOddList.value?.clear()
        _parlayList.value?.clear()

        checkBetInfoContent(betList)
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
        oddsType: OddsType?
    ) {
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()
        this.oddsType = oddsType!!
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
                getParlayOdd(matchType, gameType, mutableListOf(it)).first()
            ).apply {
                this.matchType = matchType
                this.subscribeChannelType = subscribeChannelType
                this.playCateMenuCode = playCateMenuCode
                this.outrightMatchInfo = matchInfo
            }



            betList.add(data)
            //產生串關注單
            updateBetOrderParlay(betList)
            checkBetInfoContent(betList)
            _betInfoList.postValue(Event(betList))
            if (betList.size == 1) {
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
            it.odds.toBigDecimal()
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

        return parlayBetLimitMap.map {
            parlayBetLimit = it.value.max.toInt()
            var maxBet = 9999
            val maxBetMoney = GameConfigManager.maxBetMoney ?: 9999999
            val maxCpBetMoney = GameConfigManager.maxCpBetMoney
            val maxParlayBetMoney = GameConfigManager.maxParlayBetMoney ?: 9999999

            when (matchType) {
                MatchType.PARLAY -> {
                    if (isParlayBet) {
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
                    maxBetMoney?.let { if (maxBetMoney < parlayBetLimit) maxBetMoney else parlayBetLimit }
                        ?: parlayBetLimit
            }

            //[Martin]為馬來盤＆印度計算投注上限
            if (oddsType == OddsType.MYS) {
                if (matchOddList[0].malayOdds < 0 && oddsList.size <= 1) {
                    if (maxBetMoney != null) {
                        var myMax = (maxBetMoney.div(abs(matchOddList[0].malayOdds)))!!.toInt()
                        maxBet = if (myMax < playQuota?.max!!) myMax else playQuota?.max
                    }
                }
            } else if (oddsType == OddsType.IDN) {
                if (matchOddList[0].indoOdds < 0 && oddsList.size <= 1) {
                    //印度賠付額上限
                    var indoMax = ((playQuota?.max?.toDouble()
                        ?.plus(abs(matchOddList[0].indoOdds)))!!.toInt()) - 1
                    //印度使用者投注上限
                    if (maxBetMoney != null) {
                        var indoUserMax = maxBetMoney?.div(abs(matchOddList[0].indoOdds)).toInt()
                        maxBet = if (indoUserMax < indoMax) indoUserMax else indoMax

                    }
                }
            }

            ParlayOdd(
                parlayType = it.key,
                max = maxBet,
                min = it.value.min.toInt(),
                num = it.value.num,
                odds = it.value.odds.toDouble(),
                hkOdds = it.value.hdOdds.toDouble(),
                //Martin
                malayOdds = if (oddsList.size > 1) it.value.odds.toDouble() else matchOddList[0].malayOdds,
                indoOdds = if (oddsList.size > 1) it.value.odds.toDouble() else matchOddList[0].indoOdds
            )
        }
    }


    fun saveOddsHasChanged(matchOdd: MatchOdd) {
        val hasChanged = _betInfoList.value?.peekContent()?.find {
            it.matchOdd.oddsId == matchOdd.oddsId
        }
        hasChanged?.matchOdd?.oddsHasChanged = true
        hasChanged?.matchOdd?.oddState = OddState.SAME.state
    }

    //TODO review 待刪除
    /*fun notifyBetInfoChanged() {
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
                            newBetInfoListData.betAmount = betInfoListData.betAmount
                            newBetInfoListData.pointMarked = betInfoListData.pointMarked
                            newList.add(newBetInfoListData)
                        }
                    }
                }
                checkBetInfoContent(newList)
                _betInfoList.value = Event(newList)
            }
        }
    }*/

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
                        ).first()
                    )
                    newBetInfoListData.matchType = betInfoListData.matchType
                    newBetInfoListData.input = betInfoListData.input
                    newBetInfoListData.betAmount = betInfoListData.betAmount
                    newBetInfoListData.pointMarked = betInfoListData.pointMarked
                    newList.add(newBetInfoListData)
                }
            }
        }
        checkBetInfoContent(newList)
        _betInfoList.value = Event(newList)
    }

    fun notifyBetInfoChanged(newList: MutableList<BetInfoListData>) {
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
}