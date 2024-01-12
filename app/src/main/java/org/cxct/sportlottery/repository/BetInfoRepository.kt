package org.cxct.sportlottery.repository


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddState
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.enums.SpreadState
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.bet.settledDetailList.BetInfo
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.util.BetPlayCateFunction.isEndScoreType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.betList.BetListFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.MatchOddUtil.convertToIndoOdds
import org.cxct.sportlottery.util.MatchOddUtil.convertToMYOdds
import org.cxct.sportlottery.util.parlaylimit.ParlayLimitUtil
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs


const val BET_INFO_MAX_COUNT = 10
const val BET_BASKETBALL_ENDING_SCORE_MAX_COUNT = 100


object BetInfoRepository {

    var betListTabPosition = 0 //記錄betListTab位置

    var isTouched = false

    var currentBetType: Int = BetListFragment.SINGLE
        set(value) {
            println("currentBetType:${value}")
            field = value
        }

    private val _showBetInfoSingle = MutableLiveData<Event<Boolean?>>()


    val showBetInfoSingle: LiveData<Event<Boolean?>>
        get() = _showBetInfoSingle

    //每個畫面都要觀察
    private val _betInfoList =
        MutableLiveData<Event<CopyOnWriteArrayList<BetInfoListData>>>().apply {
            value = Event(CopyOnWriteArrayList())
        }
    val betInfoList: LiveData<Event<CopyOnWriteArrayList<BetInfoListData>>>
        get() = _betInfoList

    private val _betIDList = MutableLiveData<Event<MutableList<String>>>().apply {
        value = Event(mutableListOf())
    }
    val betIDList: LiveData<Event<MutableList<String>>>
        get() = _betIDList

    private val _showBetUpperLimit = MutableLiveData<Event<Boolean>>()

    var showBetBasketballUpperLimit: MutableLiveData<Event<Boolean>> = MutableLiveData()
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

    //注单结算通知
    val settlementNotificationMsg: LiveData<Event<FrontWsEvent.BaseSportBet>>
        get() = _settlementNotificationMsg
    private val _settlementNotificationMsg = MutableLiveData<Event<FrontWsEvent.BaseSportBet>>()

    fun postSettlementNotificationMsg(sportBet: FrontWsEvent.BaseSportBet) {
        _settlementNotificationMsg.postValue(Event(sportBet))
    }

    /**
     * 0.单关
     * 1.串关
     * 2.篮球末位比分
     */
    var currentState: Int = 0

    fun setCurrentBetState(currentState: Int) {
        this.currentState = currentState
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
//        val hasMatchType = betList.find { it.matchType == MatchType.OUTRIGHT } != null

        //檢查有沒有反波膽
        val hasLcsGameType = betList.find { it.matchOdd.playCode == PlayCate.LCS.name } != null

        //檢查是否有不同的球賽種類
        val gameType = GameType.getGameType(betList.getOrNull(0)?.matchOdd?.gameType)
        val hasDiffGameType =
            betList.find { GameType.getGameType(it.matchOdd.gameType) != gameType } != null

        //檢查是否有相同賽事
        val matchIdList: MutableMap<String, MutableList<Int>> = mutableMapOf()
        betList.forEachIndexed { index, betInfoListData ->
            matchIdList[betInfoListData.matchOdd.matchId]?.add(index)
                ?: run { matchIdList[betInfoListData.matchOdd.matchId] = mutableListOf(index) }
        }

        betList.forEach {
            //parlay (是否可以参加过关，0：否，1：是)
            val cannotParlay = it.outrightMatchInfo?.parlay == 0
            //是否是冠军
            val hashOutRight = it.matchType == MatchType.OUTRIGHT
//            Timber.e("parlay: ${it.outrightMatchInfo?.parlay}, cannotParlay: $cannotParlay")
            if (cannotParlay || hasLcsGameType || hashOutRight || hasDiffGameType || (matchIdList[it.matchOdd.matchId]?.size
                    ?: 0) > 1
            ) {
                hasPointMark = true
                it.pointMarked = true
            } else {
                it.pointMarked = false
            }
        }

        gameType?.let {
            var betInfo: BetInfo? = null
            val parlayMatchOddList = betList.map { betInfoListData ->
                betInfo = betInfoListData.betInfo
                betInfoListData.matchOdd
            }.toMutableList()

            _matchOddList.postValue(parlayMatchOddList)

            if (!hasPointMark) {
                /**
                 * 原在此處有將N串1移至第一項的排序邏輯移動至ParlayLimitUtil.getCom()
                 * @see ParlayLimitUtil.getCom N串1排序
                 */
                val newParlayList = getParlayOdd(
                    MatchType.PARLAY, it, parlayMatchOddList, true, betInfo = betInfo
                ).toMutableList()

                if (!_parlayList.value.isNullOrEmpty() && _parlayList.value?.size == newParlayList.size) {
                    _parlayList.value?.forEachIndexed { index, parlayOdd ->
                        newParlayList[index].apply {
                            input = parlayOdd.input
                            betAmount = parlayOdd.betAmount
                            inputBetAmountStr = parlayOdd.inputBetAmountStr
                            singleInput = parlayOdd.singleInput
                            allSingleInput = parlayOdd.allSingleInput
                            amountError = parlayOdd.amountError
                            isInputBet = parlayOdd.isInputBet
                        }
                    }
                }
                _parlayList.postValue(newParlayList)
                _betParlaySuccess.postValue(true)
            } else {
                _parlayList.postValue(mutableListOf())
                _betParlaySuccess.postValue(false)
            }
        }
    }

    fun removeItem(oddId: String?) {
        val betList = _betInfoList.value?.peekContent() ?: CopyOnWriteArrayList()
        Timber.d("betList:${betList}")

        val item = betList.find { it.matchOdd.oddsId == oddId }
        betList.remove(item)
        if (betList.isNotEmpty()) {
            betList[0].input = null
        }
        updateQuickListManager(betList)

        val oddIDStr = oddId ?: ""
        val oddIDArray = _betIDList.value?.peekContent() ?: mutableListOf()
        oddIDArray.remove(oddIDStr)
        _betIDList.postValue(Event(oddIDArray))

        _removeItem.postValue(Event(item?.matchOdd?.matchId))
        updateBetOrderParlay(betList)
        checkBetInfoContent(betList)
        _betInfoList.postValue(Event(betList))
        if (betList.size==0){
            betListTabPosition = 0
            currentState = 0
        }
    }

    fun removeClosedPlatItem() {
        val betList = _betInfoList.value?.peekContent() ?: CopyOnWriteArrayList()

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
        if (betList.size==0){
            betListTabPosition = 0
            currentState = 0
        }
    }


    fun clear() {
        val betList = _betInfoList.value?.peekContent() ?: CopyOnWriteArrayList()
        val oddIDArray = _betIDList.value?.peekContent() ?: mutableListOf()
        betList.clear()
        oddIDArray.clear()
        _matchOddList.value?.clear()
        _parlayList.value?.clear()

        updateQuickListManager(betList)

        checkBetInfoContent(betList)
        _betIDList.postValue(Event(oddIDArray))
        _betInfoList.postValue(Event(betList))
        betListTabPosition = 0
        currentState = 0
    }

    fun switchSingleMode() {
        val betList = _betInfoList.value?.peekContent() ?: CopyOnWriteArrayList()
        var oddIDArray = _betIDList.value?.peekContent() ?: mutableListOf()
        betList.takeIf {
            it.size > 1
        }?.apply {
            val singleList = get(0)
            clear()
            add(singleList)
        }

        oddIDArray.takeIf {
            it.size > 1
        }?.apply {
            oddIDArray = subList(0, 1)
        }

        _matchOddList.value?.clear()
        _parlayList.value?.clear()

        updateQuickListManager(betList)

        checkBetInfoContent(betList)
        _betIDList.postValue(Event(oddIDArray))
        _betInfoList.postValue(Event(betList))
        betListTabPosition = 0
    }

    fun switchParlayMode() {
        val betList = _betInfoList.value?.peekContent() ?: CopyOnWriteArrayList()
        val oddIDArray = _betIDList.value?.peekContent() ?: mutableListOf()

        updateQuickListManager(betList)

        checkBetInfoContent(betList)
        _betIDList.postValue(Event(oddIDArray))
        _betInfoList.postValue(Event(betList))
        betListTabPosition = 0
    }

    /**
     * 点击赔率按钮加入投注清单，并产生串关注单
     * 不同投注能不能串的逻辑在这里面
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
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        betInfo: BetInfo? = null
    ) {
//        Timber.v("Bill====>betInfo:${betInfo}")
        val betList = _betInfoList.value?.peekContent() ?: CopyOnWriteArrayList()
        oddsType?.let {
            this.oddsType = it
        }

        //如果当前选择的是篮球末位比分
        if (playCateCode.isEndScoreType()) {
            //注单中选择的篮球末位比分的个数
            val basketballCount =
                betList.count { it.matchOdd.playCode.isEndScoreType()}
            //如果当前选择的注单数量大于100个
//            Timber.d("basketballCount:${basketballCount}")
            if (basketballCount >= BET_BASKETBALL_ENDING_SCORE_MAX_COUNT) {
                showBetBasketballUpperLimit.postValue(Event(true))
                return
            }
        } else {
            //如果当前选择的不是篮球末位比分
            //选择的篮球末位比分的个数
            val basketballCount =
                betList.count {
                    it.matchOdd.playCode.isEndScoreType()
                }
            //除了篮球末位比分以外的数量
            val otherCount = betList.size - basketballCount
//            Timber.d("basketballCount:${basketballCount} betList:${betList} otherCount:${otherCount}")
            if (otherCount >= BET_INFO_MAX_COUNT) {
                _showBetUpperLimit.postValue(Event(true))
                return
            }
        }

        val betInfoMatchOdd = MatchOddUtil.transfer(
            matchType = matchType,
            gameType = gameType.key,
            playCateCode = playCateCode,
            playCateName = playCateName,
            playName = playName,
            matchInfo = matchInfo,
            odd = odd
        ) ?: return

        val data = BetInfoListData(
            betInfoMatchOdd,
            getParlayOdd(matchType, gameType, mutableListOf(betInfoMatchOdd), betInfo = betInfo).first(),
            betPlayCateNameMap,
        ).apply {
            this.matchType = matchType
            this.subscribeChannelType = subscribeChannelType
            this.playCateMenuCode = playCateMenuCode
            this.outrightMatchInfo = matchInfo
            this.betInfo = betInfo
        }

//            Timber.d("==Bet Refactor==> _betIDList.size():${_betIDList.value?.peekContent()?.size}")
        val oddIDArray = _betIDList.value?.peekContent() ?: mutableListOf()

        //是不是同一场比赛
        val currentMatchName = playCateCode + betInfoMatchOdd.awayName + betInfoMatchOdd.homeName
        var lastMatchName: String? = null
        var lastPlayCode: String? = null
        if (betList.isNotEmpty()) {
            val lastMatchOdd = betList.last().matchOdd
            lastMatchName = lastMatchOdd.playCode + lastMatchOdd.awayName + lastMatchOdd.homeName
            lastPlayCode = lastMatchOdd.playCode
        }
        val isSameMatch = (currentMatchName == lastMatchName) || (lastMatchName == null)
        Timber.d("isSameMatch:${isSameMatch} currentMatchName:${currentMatchName} lastMatchName:${lastMatchName} playCateCode:$playCateCode")
        //篮球末位比分
        //末位比分区分总分和节比分共5种，需要判断是不是末位比分里面同一种
        val isSamePlayCodeThanBefore = playCateCode == lastPlayCode
        if (playCateCode.isEndScoreType()) {
            if (isSameMatch && isSamePlayCodeThanBefore) {
                Timber.d("篮球末位比分模式")
                oddIDArray.add(betInfoMatchOdd.oddsId)
                betList.add(data)
                betList[0].input = null
            } else {
                oddIDArray.clear()
                betList.clear()
                oddIDArray.add(betInfoMatchOdd.oddsId)
                betList.add(data)
            }
            setCurrentBetState(BetListFragment.BASKETBALL_ENDING_CARD)
            currentBetType = BetListFragment.BASKETBALL_ENDING_CARD
        } else {
            Timber.d("currentState:${currentState}")
            if (currentState != 1) {
                setCurrentBetState(0)
            }
            if (currentState == 0) {
                //单注模式
                Timber.d("单注模式")
                oddIDArray.clear()
                betList.clear()
                oddIDArray.add(betInfoMatchOdd.oddsId)
                betList.add(data)
                currentBetType = BetListFragment.SINGLE
            } else if (currentState == 1) {
                Timber.d("串关模式")
                //串关投注
                oddIDArray.add(betInfoMatchOdd.oddsId)
                betList.add(data)
                currentBetType = BetListFragment.PARLAY
            }
        }

        _betIDList.postValue(Event(oddIDArray))
        updateQuickListManager(betList)

        //產生串關注單
        updateBetOrderParlay(betList)
        checkBetInfoContent(betList)
        _betInfoList.postValue(Event(betList))

        //单注才弹出购物车
        if (betList.size == 1) {
            _showBetInfoSingle.postValue(Event(true))
        }
    }

    /**
     * @param isParlayBet 2021/10/29新增, gameType為GameType.PARLAY時不代表該投注為串關投注, 僅由組合後產生的投注才是PARLAY
     * @param betInfo 2022/8/4 賠率上下限統一改為/api/front/match/bet/info取得
     */
    private fun getParlayOdd(
        matchType: MatchType,
        gameType: GameType,
        matchOddList: MutableList<MatchOdd>,
        isParlayBet: Boolean = false,
        betInfo: BetInfo? = null
    ): List<ParlayOdd> {
        val oddsList = matchOddList.map {
            Pair(it.odds.toBigDecimal(), it.isOnlyEUType)
        }

        val oddsIndexList = oddsList.map {
            oddsList.indexOf(it)
        }

        val parlayComList = ParlayLimitUtil.getCom(oddsIndexList.toIntArray())

        val maxDefaultBigDecimal = BigDecimal(9999999)
        val minDefaultBigDecimal = BigDecimal(0)

        /** 不使用maxParlayBetMoney, maxParlayPayout, minParlayBetMoney (By Mark)
         * 串關的限額判斷改為根據串關的所有賽事的限額去比對
        maxBetMoney 下注上限取最小的,
        maxPayout 賠付額上限取最小的,
        minBetMoney 最低下注金額取最大的,
        作為該張串關單的最高下注限額/最大賠付額/最低下注限額
         */
        val betList = _betInfoList.value?.peekContent() ?: mutableListOf()
        val betInfoList = betList.map { it.betInfo }
        val startTime = System.nanoTime()
        val maxBetMoneyTakeMin = betInfoList.minOfOrNull { it?.maxBetMoney ?: maxDefaultBigDecimal }
        val maxPayoutTakeMin = betInfoList.minOfOrNull { it?.maxPayout ?: maxDefaultBigDecimal }
        val minBetMoneyTakeMax = betInfoList.maxOfOrNull { it?.minBetMoney ?: minDefaultBigDecimal }
//        Timber.e("costTime: ${System.nanoTime() - startTime}")
        val parlayBetLimitMap = ParlayLimitUtil.getParlayLimit(
            oddsList, parlayComList, maxBetMoneyTakeMin, minBetMoneyTakeMax
        )

        return parlayBetLimitMap.map {
            var maxBet: BigDecimal
            val maxPayout = betInfo?.maxPayout ?: maxDefaultBigDecimal
            val maxCpPayout = betInfo?.maxCpPayout ?: maxDefaultBigDecimal
            val maxBetMoney = betInfo?.maxBetMoney ?: maxDefaultBigDecimal
            val maxCpBetMoney = betInfo?.maxCpBetMoney ?: maxDefaultBigDecimal
            val maxParlayPayout = maxPayoutTakeMin ?: maxDefaultBigDecimal
            var maxParlayBetMoney = maxBetMoneyTakeMin ?: maxDefaultBigDecimal
            val minBet: BigDecimal
            val minBetMoney = betInfo?.minBetMoney ?: minDefaultBigDecimal
            val minCpBetMoney = betInfo?.minCpBetMoney ?: minDefaultBigDecimal
            val minParlayBetMoney = minBetMoneyTakeMax ?: minDefaultBigDecimal
            if (it.value.num > 1) {
                //大於1 即為組合型串關 最大下注金額有特殊規則：賠付額上限計算方式
                val odds = if (it.value.isOnlyEUType) {
                    //賠付額計算需扣除本金, 此處為串關有幾注就要
                    it.value.maxOdds - BigDecimal(1)
                } else {
                    it.value.maxHdOdds
                }
                val parlayPayout = ArithUtil.div(maxParlayPayout, odds, 2, RoundingMode.DOWN)
                val maxParlayBet = if (maxParlayBetMoney == BigDecimal(0)) {
                    //如果 maxParlayBetMoney 為 0 使用最大賠付額
                    parlayPayout
                } else {
                    //投注額和賠付額取小計算
                    maxParlayBetMoney.min(parlayPayout)
                }
                maxBet = maxParlayBet
                minBet = minParlayBetMoney
            } else {
                val payout: BigDecimal
                betInfo?.maxBetMoney?.let {
                    maxParlayBetMoney = it
                }
                //根據賽事類型的投注上限
                val matchTypeMaxBetMoney = when {
                    matchType == MatchType.PARLAY && isParlayBet -> {
                        payout = maxParlayPayout
                        maxParlayBetMoney
                    }

                    matchType == MatchType.OUTRIGHT -> {
                        //冠軍賠付額
                        payout = maxCpPayout
                        maxCpBetMoney
                    }

                    else -> {
                        //一般賠付額
                        payout = maxPayout
                        maxBetMoney
                    }
                }
                //賠付額上限計算投注限額
                val odds = if (it.value.isOnlyEUType) {
                    //賠付額計算需扣除本金, 此處為串關有幾注就要扣幾個本金
                    it.value.maxOdds - BigDecimal(1)
                } else {
                    it.value.maxHdOdds
                }
                val oddsPayout = ArithUtil.div(payout, odds, 2, RoundingMode.DOWN)
                maxBet = if (matchTypeMaxBetMoney == BigDecimal(0)) {
                    //如果 matchTypeMaxBetMoney 為 0 使用最大賠付額
                    oddsPayout
                } else {
                    //用戶投注限額與賠付額計算投注限額取小
                    oddsPayout.min(matchTypeMaxBetMoney)
                }
                minBet = when {
                    matchType == MatchType.PARLAY && isParlayBet -> minParlayBetMoney
                    matchType == MatchType.OUTRIGHT -> minCpBetMoney
                    else -> minBetMoney
                }

                //[Martin]為馬來盤＆印度計算投注上限
                if (oddsType == OddsType.MYS && !it.value.isOnlyEUType) {
                    if ((matchOddList.getOrNull(0)?.malayOdds ?: 0.0) < 0.0 && oddsList.size <= 1) {
                        //馬來盤使用者投注上限
                        maxBet = (ArithUtil.div(
                            maxBetMoney,
                            abs(matchOddList.getOrNull(0)?.malayOdds ?: 0.0).toBigDecimal(),
                            2,
                            RoundingMode.DOWN
                        ))
                    }
                } else if (oddsType == OddsType.IDN && !it.value.isOnlyEUType) {
                    if ((matchOddList.getOrNull(0)?.indoOdds ?: 0.0) < 0.0 && oddsList.size <= 1) {
                        //印度使用者投注上限
                        maxBet = (ArithUtil.div(
                            maxBetMoney,
                            abs(matchOddList.getOrNull(0)?.indoOdds ?: 0.0).toBigDecimal(),
                            2,
                            RoundingMode.DOWN
                        ))
                    }
                }
            }
            ParlayOdd(
                parlayType = it.key,
                max = maxBet.toDouble().toLong(),
                min = minBet.toDouble().toLong(),
                num = it.value.num,
                odds = it.value.odds.toDouble(),
                hkOdds = it.value.hdOdds.toDouble(),
                //Martin
                malayOdds = if (oddsList.size > 1) it.value.odds.toDouble() else matchOddList.getOrNull(
                    0
                )?.malayOdds ?: 0.0,
                indoOdds = if (oddsList.size > 1) it.value.odds.toDouble() else matchOddList.getOrNull(
                    0
                )?.indoOdds ?: 0.0
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


    fun notifyBetInfoChanged() {
        val updateBetInfoList = _betInfoList.value?.peekContent()

        if (updateBetInfoList.isNullOrEmpty()) return

        updateBetInfoList.toList().forEach { betInfoListData ->
            betInfoListData.matchType?.let { matchType ->
                val gameType = GameType.getGameType(betInfoListData.matchOdd.gameType)
                gameType?.let {
                    betInfoListData.parlayOdds = getParlayOdd(
                        matchType,
                        gameType,
                        mutableListOf(betInfoListData.matchOdd),
                        betInfo = betInfoListData.betInfo
                    ).first()
                }
            }
        }

        updateQuickListManager(updateBetInfoList)

        checkBetInfoContent(updateBetInfoList)
        updateBetOrderParlay(updateBetInfoList)
        _betInfoList.postValue(Event(updateBetInfoList))
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

    private fun updateQuickListManager(betList: MutableList<BetInfoListData>) {
        //更新快捷投注項選中list
        QuickListManager.setQuickSelectedList(betList.map { bet -> bet.matchOdd.oddsId }
            .toMutableList())
    }

    fun updateMatchOdd(changeEvent: Any) {
        val newList: MutableList<Odd> = mutableListOf()
        when (changeEvent) {
            is OddsChangeEvent -> {
                changeEvent.odds.toMap().forEach { map ->
                    val value = map.value
                    value?.forEach { odd ->
                        odd.let {
                            newList.add(it)
                        }
                    }
                }
            }

            is MatchOddsChangeEvent -> {
                for ((_, value) in changeEvent.odds ?: mapOf()) {
                    value.odds?.toList()?.forEach { odd ->
                        odd?.let { o ->
                            newList.add(o)
                        }
                    }
                }
            }
        }
        betInfoList.value?.peekContent()?.toArray()?.forEach {it as BetInfoListData
            updateItem(it.matchOdd, newList)
        }
        notifyBetInfoChanged()

    }

    private fun updateItem(
        oldItem: MatchOdd, newList: List<Odd>
    ) {
        newList.firstOrNull { it.id == oldItem.oddsId }?.let {
                //若賠率關閉則賠率不做高亮變化
                it.status.let { status -> oldItem.status = status }
                //賠率為啟用狀態時才去判斷是否有賠率變化
                var currentOddsType =
                    MultiLanguagesApplication.mInstance.mOddsType.value ?: OddsType.HK
                if (it.odds == it.malayOdds) currentOddsType = OddsType.EU
                if (oldItem.status == BetStatus.ACTIVATED.code) {
                    oldItem.oddState = getOddState(
                        getOdds(
                            oldItem, currentOddsType
                        ), it
                    )

                    if (oldItem.oddState != OddState.SAME.state) oldItem.oddsHasChanged =
                        true
                }

                oldItem.spreadState = getSpreadState(oldItem.spread, it.spread ?: "")

                if (oldItem.status == BetStatus.ACTIVATED.code) {
                    it.odds.let { odds -> oldItem.odds = odds ?: 0.0 }
                    it.hkOdds.let { hkOdds -> oldItem.hkOdds = hkOdds ?: 0.0 }
                    it.indoOdds.let { indoOdds -> oldItem.indoOdds = indoOdds ?: 0.0 }
                    it.malayOdds.let { malayOdds ->
                        oldItem.malayOdds = malayOdds ?: 0.0
                    }
                    it.spread.let { spread -> oldItem.spread = spread ?: "" }
                }

                //從socket獲取後 賠率有變動並且投注狀態開啟時 需隱藏錯誤訊息
                if (oldItem.oddState != OddState.SAME.state && oldItem.status == BetStatus.ACTIVATED.code) {
                    oldItem.betAddError = null
                }

            }
    }

    private fun getOddState(
        oldItemOdds: Double, newOdd: Odd
    ): Int {
        //馬來盤、印尼盤為null時自行計算
        var newMalayOdds = 0.0
        var newIndoOdds = 0.0

        newOdd.hkOdds?.let {hkOddsNotNull ->
            newMalayOdds = hkOddsNotNull.convertToMYOdds()

            newIndoOdds = hkOddsNotNull.convertToIndoOdds()
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

    private fun getSpreadState(oldSpread: String, newSpread: String): Int = when {
        newSpread != oldSpread -> SpreadState.DIFFERENT
        else -> SpreadState.SAME
    }
    fun updateDiscount(newDiscountByGameTypeList: List<FrontWsEvent.DiscountByGameTypeVO>?) {


        betInfoList.value?.peekContent()?.toList()?.forEach { betInfoListData ->
            newDiscountByGameTypeList?.firstOrNull { it.gameType == betInfoListData.matchOdd.gameType }?.discount?.toBigDecimalOrNull()
                ?.let {
                    betInfoListData.matchOdd.updateDiscount(it)
                }
        }
        notifyBetInfoChanged()
    }
}