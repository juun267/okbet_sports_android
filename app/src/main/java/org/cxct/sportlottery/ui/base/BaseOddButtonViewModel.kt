package org.cxct.sportlottery.ui.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.enum.SpreadState
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.add.BetAddErrorData
import org.cxct.sportlottery.network.bet.add.BetAddRequest
import org.cxct.sportlottery.network.bet.add.Stake
import org.cxct.sportlottery.network.bet.add.betReceipt.BetAddResult
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.error.BetAddError
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.match_odds_lock.MatchOddsLockEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.common.PlayCateMapItem
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*


abstract class BaseOddButtonViewModel(
    val androidContext: Application,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository
) : BaseViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    protected val playCateMappingList by lazy {
        val json = LocalJsonUtil.getLocalJson(
            MultiLanguagesApplication.appContext,
            "localJson/PlayCateMapping.json"
        )
        json.fromJson<List<PlayCateMapItem>>() ?: listOf()
    }


    val showBetInfoSingle = betInfoRepository.showBetInfoSingle

    val betInfoList = betInfoRepository.betInfoList

    val showOddsChangeWarn get() = betInfoRepository.showOddsChangeWarn

    val showOddsCloseWarn get() = betInfoRepository.showOddsCloseWarn

    val hasBetPlatClose get() = betInfoRepository.hasBetPlatClose

    val oddsType: LiveData<OddsType> = loginRepository.mOddsType

    val betAddResult: LiveData<Event<BetAddResult?>>
        get() = _betAddResult

    protected val mUserMoney = MutableLiveData<Double?>()
    val userMoney: LiveData<Double?> //使用者餘額
        get() = mUserMoney

    private val _betAddResult = MutableLiveData<Event<BetAddResult?>>()

    val betParlaySuccess: LiveData<Boolean>
        get() = betInfoRepository.betParlaySuccess

    fun getMoney() {
        if (isLogin.value == false) return

        viewModelScope.launch {
            val userMoneyResult = doNetwork(androidContext) {
                OneBoSportApi.userService.getMoney()
            }
            mUserMoney.postValue(userMoneyResult?.money)
        }
    }

    fun saveOddsType(oddsType: OddsType) {
        loginRepository.sOddsType = oddsType.code
        loginRepository.mOddsType.postValue(oddsType)
    }

    fun getOddsType() {
        loginRepository.mOddsType.postValue(
            when (loginRepository.sOddsType) {
                OddsType.EU.code -> OddsType.EU
                OddsType.HK.code -> OddsType.HK
                else -> OddsType.EU
            }
        )
    }

    fun updateMatchBetList(
        matchType: MatchType,
        gameType: GameType,
        playCateName: String,
        matchInfo: MatchInfo,
        odd: org.cxct.sportlottery.network.odds.Odd,
        subscribeChannelType: ChannelType,
        playCateMenuCode: String? = null
    ) {
        val betItem = betInfoRepository.betInfoList.value?.peekContent()
            ?.find { it.matchOdd.oddsId == odd.id }

        if (betItem == null) {
            matchInfo.let {
                betInfoRepository.addInBetInfo(
                    matchType = matchType,
                    gameType = gameType,
                    playCateName = playCateName,
                    playName = odd.nameMap?.get(LanguageManager.getSelectLanguage(androidContext).key) ?: odd.name ?: "",
                    matchInfo = matchInfo,
                    odd = odd,
                    subscribeChannelType = subscribeChannelType,
                    playCateMenuCode = playCateMenuCode
                )
            }
        } else {
            odd.id?.let { removeBetInfoItem(it) }
        }
    }

    fun updateMatchBetListForOutRight(
        matchType: MatchType,
        gameType: GameType,
        matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd,
        odd: org.cxct.sportlottery.network.odds.Odd
    ) {
        val outrightCateName = matchOdd.dynamicMarkets[odd.outrightCateKey].let {
            when (LanguageManager.getSelectLanguage(androidContext)) {
                LanguageManager.Language.ZH -> {
                    it?.zh
                }
                else -> {
                    it?.en
                }
            }
        }

        val betItem = betInfoRepository.betInfoList.value?.peekContent()
            ?.find { it.matchOdd.oddsId == odd.id }

        if (betItem == null) {
            matchOdd.matchInfo?.let {
                betInfoRepository.addInBetInfo(
                    matchType = matchType,
                    gameType = gameType,
                    playCateName = outrightCateName
                        ?: "",
                    playName = odd.nameMap?.get(LanguageManager.getSelectLanguage(androidContext).key) ?: odd.name ?: "",
                    matchInfo = matchOdd.matchInfo,
                    odd = odd,
                    subscribeChannelType = ChannelType.HALL,
                    playCateMenuCode = PlayCate.OUTRIGHT.value
                )
            }
        } else {
            odd.id?.let { removeBetInfoItem(it) }
        }
    }

    fun updateMatchOddForParlay(matchOdd: MatchOddsChangeEvent) {
        val newList: MutableList<org.cxct.sportlottery.network.odds.Odd> =
            mutableListOf()
        for ((_, value) in matchOdd.odds ?: mapOf()) {
            value.odds?.forEach { odd ->
                odd?.let { o ->
                    newList.add(o)
                }
            }
        }
        updateBetInfoListByMatchOddChange(newList)
    }

    fun updateMatchOddForParlay(
        betAddErrorDataList: List<BetAddErrorData>,
        betAddError: BetAddError
    ) {
        val newList: MutableList<org.cxct.sportlottery.network.odds.Odd> = mutableListOf()
        betAddErrorDataList.forEach { betAddErrorData ->
            betAddErrorData.let { data ->
                data.status?.let { status ->
                    val newOdd = org.cxct.sportlottery.network.odds.Odd(
                        extInfoMap = null,
                        id = data.id,
                        name = null,
                        odds = data.odds,
                        hkOdds = data.hkOdds,
                        producerId = data.producerId,
                        spread = data.spread,
                        status = status,
                    )
                    newList.add(newOdd)
                }
            }
        }

        betInfoRepository.matchOddList.value?.forEach {
            updateItemForBetAddError(it, newList, betAddError)
        }

        updateBetInfoListByMatchOddChange(newList)
    }

    fun updateLockMatchOdd(matchOddsLock: MatchOddsLockEvent) {
        betInfoRepository.betInfoList.value?.peekContent()
            ?.find { it.matchOdd.matchId == matchOddsLock.matchId }?.matchOdd?.status = BetStatus.LOCKED.code

        betInfoRepository.notifyBetInfoChanged()
    }

    fun updateMatchOdd(changeEvent: Any) {
        val newList: MutableList<org.cxct.sportlottery.network.odds.Odd> = mutableListOf()
        when (changeEvent) {
            is OddsChangeEvent -> {
                changeEvent.odds?.forEach { map ->
                    val value = map.value
                    value.forEach { odd ->
                        odd?.let {
                            val newOdd = org.cxct.sportlottery.network.odds.Odd(
                                extInfoMap = null,
                                id = odd.id,
                                name = null,
                                odds = odd.odds,
                                hkOdds = odd.hkOdds,
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
//        updateNewItem(newList)
        betInfoRepository.betInfoList.value?.peekContent()?.forEach {
            updateItem(it.matchOdd, newList)
        }
        betInfoRepository.notifyBetInfoChanged()

    }

    fun updateMatchOdd(betAddErrorDataList: List<BetAddErrorData>, betAddError: BetAddError) {
        val newList: MutableList<org.cxct.sportlottery.network.odds.Odd> = mutableListOf()
        betAddErrorDataList.forEach { betAddErrorData ->
            betAddErrorData.let { data ->
                data.status?.let { status ->
                    val newOdd = org.cxct.sportlottery.network.odds.Odd(
                        extInfoMap = null,
                        id = data.id,
                        name = null,
                        odds = data.odds,
                        hkOdds = data.hkOdds,
                        producerId = data.producerId,
                        spread = data.spread,
                        status = status,
                    )
                    newList.add(newOdd)
                }
            }
        }

        betInfoRepository.betInfoList.value?.peekContent()?.forEach {
            updateItemForBetAddError(it.matchOdd, newList, betAddError)
        }
        betInfoRepository.notifyBetInfoChanged()
    }

    /**
     * 新的投注單沒有單一下注, 一次下注一整單, 下注完後不管成功失敗皆清除所有投注單內容
     * @date 20210730
     */
    fun addBetList(
        normalBetList: List<BetInfoListData>,
        parlayBetList: List<ParlayOdd>,
        oddsType: OddsType
    ) {

        //一般注單
        val matchList: MutableList<Odd> = mutableListOf()
        normalBetList.forEach {
            matchList.add(Odd(it.matchOdd.oddsId, getOdds(it.matchOdd, oddsType), it.betAmount))
        }

        //串關注單
        val parlayList: MutableList<Stake> = mutableListOf()
        parlayBetList.forEach {
            if (it.betAmount > 0) {
                parlayList.add(Stake(TextUtil.replaceCByParlay(it.parlayType), it.betAmount))
            }
        }

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.betService.addBet(
                    BetAddRequest(
                        matchList,
                        parlayList,
                        1,
                        oddsType.code,
                        2
                    )
                )
            }

            result?.receipt?.singleBets?.forEach { s ->
                s.matchOdds?.forEach { m ->
                    s.matchType = normalBetList.find { betInfoListData ->
                        betInfoListData.matchOdd.oddsId == m.oddsId
                    }?.matchType
                }
            }

            Event(result).getContentIfNotHandled()?.success?.let {
                _betAddResult.postValue(Event(result))
                if (it) {
                    betInfoRepository.clear()
                }
                updateTransNum()
            }

        }
    }

    //更新交易狀況數量
    private fun updateTransNum () {
        viewModelScope.launch {
            doNetwork(androidContext) {
                loginRepository.getTransNum()
            }
        }
    }

    fun addBetSingle(stake: Double, betInfoListData: BetInfoListData) {
        val parlayType =
            if (betInfoListData.matchType == MatchType.OUTRIGHT) MatchType.OUTRIGHT.postValue else betInfoListData.parlayOdds?.parlayType

        val request = BetAddRequest(
            listOf(
                Odd(
                    betInfoListData.matchOdd.oddsId,
                    getOdds(betInfoListData.matchOdd, oddsType.value ?: OddsType.EU),
                    stake
                )
            ),
            listOf(Stake(parlayType ?: "", stake)),
            1,
            oddsType.value?.code ?: OddsType.EU.code,
            2
        )

        viewModelScope.launch {
            val result = getBetApi(request)
            _betAddResult.postValue(Event(result))
            result?.receipt?.singleBets?.firstOrNull()?.matchType = betInfoListData.matchType
            Event(result).getContentIfNotHandled()?.success?.let {
                if (it) {
                    afterBet(betInfoListData.matchType, result)
                }
                updateTransNum()
            }
        }
    }

    fun saveOddsHasChanged(matchOdd: org.cxct.sportlottery.network.bet.info.MatchOdd) {
        betInfoRepository.saveOddsHasChanged(matchOdd)
    }

    fun removeBetInfoItem(oddId: String?) {
        betInfoRepository.removeItem(oddId)
    }

    fun removeBetInfoSingle() {
        if (betInfoRepository.showBetInfoSingle.value?.peekContent() == true)
            betInfoRepository.clear()
    }

    fun removeBetInfoItemAndRefresh(oddId: String) {
        removeBetInfoItem(oddId)
        if (betInfoRepository.betInfoList.value?.peekContent()?.size != 0) {
            getBetInfoListForParlay()
        }
    }

    fun removeClosedPlatBetInfo() {
        betInfoRepository.removeClosedPlatItem()
    }

    fun removeBetInfoAll() {
        betInfoRepository.clear()
    }

    fun getBetInfoListForParlay() {
        betInfoRepository.addInBetInfoParlay()
    }

    fun addInBetInfo() {
        betInfoRepository.addInBetInfo()
    }

    protected fun getOddState(
        oldItemOdds: Double,
        newOdd: org.cxct.sportlottery.network.odds.Odd
    ): Int {
        val odds = when (loginRepository.mOddsType.value) {
            OddsType.EU -> newOdd.odds
            OddsType.HK -> newOdd.hkOdds
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

    protected fun Map<String, List<org.cxct.sportlottery.network.odds.Odd?>?>.filterPlayCateSpanned(
        gameType: String?
    ): Map<String, List<org.cxct.sportlottery.network.odds.Odd?>?> {
        return this.mapValues { map ->
            val playCateMapItem = playCateMappingList.find {
                it.gameType == gameType && it.playCateCode == map.key
            }

            map.value?.filterIndexed { index, _ ->
                index < playCateMapItem?.playCateNum ?: 0
            }
        }
    }

    protected fun Map<String, List<org.cxct.sportlottery.network.odds.Odd?>?>.splitPlayCate(): Map<String, List<org.cxct.sportlottery.network.odds.Odd?>?> {
        val splitMap = mutableMapOf<String, List<org.cxct.sportlottery.network.odds.Odd?>?>()

        this.forEach { oddsMap ->
            when (oddsMap.key) {
                PlayCate.SINGLE_OU.value -> {
                    splitMap[PlayCate.SINGLE_OU_O.value] =
                        listOf(oddsMap.value?.get(0), oddsMap.value?.get(2), oddsMap.value?.get(4))

                    splitMap[PlayCate.SINGLE_OU_U.value] =
                        listOf(oddsMap.value?.get(1), oddsMap.value?.get(3), oddsMap.value?.get(5))
                }

                PlayCate.SINGLE_BTS.value -> {
                    splitMap[PlayCate.SINGLE_BTS_Y.value] =
                        listOf(oddsMap.value?.get(0), oddsMap.value?.get(2), oddsMap.value?.get(4))

                    splitMap[PlayCate.SINGLE_BTS_N.value] =
                        listOf(oddsMap.value?.get(1), oddsMap.value?.get(3), oddsMap.value?.get(5))
                }

                else -> {
                    splitMap[oddsMap.key] = oddsMap.value
                }
            }
        }

        return splitMap
    }

    protected fun Map<String, List<org.cxct.sportlottery.network.odds.Odd?>?>.sortPlayCate(): Map<String, List<org.cxct.sportlottery.network.odds.Odd?>?> {
        val sortMap = mutableMapOf<String, List<org.cxct.sportlottery.network.odds.Odd?>?>()

        this.forEach { oddsMap ->
            if (oddsMap.key.contains(PlayCate.SINGLE.value)) {
                val oddList = oddsMap.value?.toMutableList()

                oddList?.indexOf(
                    oddList.find {
                        it?.nameMap?.get(LanguageManager.Language.EN.key)?.contains("Draw") ?: false
                    }
                )?.let {
                    oddList.add(oddList.size - 1, oddList.removeAt(it))
                }

                sortMap[oddsMap.key] = oddList
            } else {
                sortMap[oddsMap.key] = oddsMap.value
            }
        }

        return sortMap
    }

    protected fun Map<String, List<org.cxct.sportlottery.network.odds.Odd?>?>.toMutableFormat(): MutableMap<String, MutableList<org.cxct.sportlottery.network.odds.Odd?>> {
        return this.mapValues { map ->
            map.value?.toMutableList() ?: mutableListOf()
        }.toMutableMap()
    }

    protected fun MatchOdd.updateOddStatus() {
        this.oddsMap.forEach {
            it.value.filterNotNull().forEach { odd ->

                odd.status = when {
                    (it.value.filterNotNull()
                        .all { mOdd -> mOdd.status == null || mOdd.status == BetStatus.DEACTIVATED.code }) -> BetStatus.DEACTIVATED.code

                    (it.value.filterNotNull()
                        .any { mOdd -> mOdd.status == null || mOdd.status == BetStatus.DEACTIVATED.code } && odd.status == BetStatus.DEACTIVATED.code) -> BetStatus.LOCKED.code

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

    protected fun org.cxct.sportlottery.network.odds.detail.MatchOdd.updateOddStatus() {
        this.odds.forEach {
            it.value.odds.filterNotNull().forEach { odd ->

                odd.status = when {
                    (it.value.odds.filterNotNull()
                        .all { mOdd -> mOdd.status == null || mOdd.status == BetStatus.DEACTIVATED.code }) -> BetStatus.DEACTIVATED.code

                    (it.value.odds.filterNotNull()
                        .any { mOdd -> mOdd.status == null || mOdd.status == BetStatus.DEACTIVATED.code } && odd.status == BetStatus.DEACTIVATED.code) -> BetStatus.LOCKED.code

                    else -> odd.status
                }
            }
        }
    }

    private fun getSpreadState(oldSpread: String, newSpread: String): Int =
        when {
            newSpread != oldSpread -> SpreadState.DIFFERENT.state
            else -> SpreadState.SAME.state
        }


    private fun updateBetInfoListByMatchOddChange(newListFromSocket: List<org.cxct.sportlottery.network.odds.Odd>) {
        betInfoRepository.matchOddList.value?.forEach {
            updateItem(it, newListFromSocket)
        }
        getBetInfoListForParlay()
    }

    private fun updateItem(
        oldItem: org.cxct.sportlottery.network.bet.info.MatchOdd,
        newList: List<org.cxct.sportlottery.network.odds.Odd>
    ) {
        for (newItem in newList) {
            try {
                newItem.let {
                    if (it.id == oldItem.oddsId) {
                        //若賠率關閉則賠率不做高亮變化
                        newItem.status.let { status -> oldItem.status = status }

                        //賠率為啟用狀態時才去判斷是否有賠率變化
                        if (oldItem.status == BetStatus.ACTIVATED.code) {
                            oldItem.oddState = getOddState(
                                getOdds(
                                    oldItem,
                                    loginRepository.mOddsType.value ?: OddsType.EU
                                ), newItem
                            )

                            if (oldItem.oddState != OddState.SAME.state)
                                oldItem.oddsHasChanged = true
                        }

                        oldItem.spreadState = getSpreadState(oldItem.spread, it.spread ?: "")

                        if (oldItem.status == BetStatus.ACTIVATED.code) {
                            newItem.odds.let { odds -> oldItem.odds = odds ?: 0.0 }
                            newItem.hkOdds.let { hkOdds -> oldItem.hkOdds = hkOdds ?: 0.0 }
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

    private fun updateNewItem(updateList: MutableList<org.cxct.sportlottery.network.odds.Odd>) {
        val updateBetInfoList = betInfoRepository.betInfoList.value?.peekContent()
        if (updateBetInfoList.isNullOrEmpty()) return

        val newList = mutableListOf<BetInfoListData>()
        updateBetInfoList.forEach { betInfoListData ->
            betInfoListData.matchType?.let { matchType ->
                //TODO Dean : review
                val gameType = GameType.getGameType(betInfoListData.matchOdd.gameType)
                gameType?.let {
                    val newBetInfoListData = BetInfoListData(
                        betInfoListData.matchOdd.copy(),
                        betInfoRepository.getParlayOdd(
                            matchType,
                            gameType,
                            mutableListOf(betInfoListData.matchOdd)
                        ).first()
                    )

                    newBetInfoListData.matchType = betInfoListData.matchType
                    newBetInfoListData.input = betInfoListData.input
                    newBetInfoListData.betAmount = betInfoListData.betAmount
                    newBetInfoListData.pointMarked = betInfoListData.pointMarked

                    val newMatchOdd = newBetInfoListData.matchOdd

                    for (newItem in updateList) {
                        try {
                            newItem.let {
                                if (it.id == newMatchOdd.oddsId) {
                                    newMatchOdd.oddState = getOddState(
                                        getOdds(
                                            newMatchOdd,
                                            loginRepository.mOddsType.value ?: OddsType.EU
                                        ), newItem
                                    )

                                    newMatchOdd.spreadState =
                                        getSpreadState(newMatchOdd.spread, it.spread ?: "")

                                    newItem.status.let { status -> newMatchOdd.status = status }

                                    if (newMatchOdd.status == BetStatus.ACTIVATED.code) {
                                        newItem.odds.let { odds -> newMatchOdd.odds = odds ?: 0.0 }
                                        newItem.hkOdds.let { hkOdds ->
                                            newMatchOdd.hkOdds = hkOdds ?: 0.0
                                        }
                                        newItem.spread.let { spread ->
                                            newMatchOdd.spread = spread ?: ""
                                        }
                                    }

                                    //從socket獲取後 賠率有變動並且投注狀態開啟時 需隱藏錯誤訊息
                                    if (newMatchOdd.oddState != OddState.SAME.state &&
                                        newMatchOdd.status == BetStatus.ACTIVATED.code
                                    ) {
                                        newMatchOdd.betAddError = null
                                    }

                                }
                            }
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }

                    newList.add(newBetInfoListData)

                    /*betInfoListData.parlayOdds =
                        getParlayOdd(matchType, gameType, mutableListOf(betInfoListData.matchOdd)).firstOrNull()
                    betInfoListData.matchOdd = betInfoListData.matchOdd*/
                }
            }
        }
        betInfoRepository.notifyBetInfoChanged(newList)
    }

    private fun updateItemForBetAddError(
        oldItem: org.cxct.sportlottery.network.bet.info.MatchOdd,
        newList: List<org.cxct.sportlottery.network.odds.Odd>,
        betAddError: BetAddError
    ) {
        for (newItem in newList) {
            //每次都先把字串清空
            oldItem.betAddError = null

            try {
                newItem.let {
                    if (it.id == oldItem.oddsId) {
                        if (betAddError == BetAddError.ODDS_HAVE_CHANGED) {
                            oldItem.oddState = getOddState(
                                getOdds(
                                    oldItem,
                                    loginRepository.mOddsType.value ?: OddsType.EU
                                ), newItem
                            )

                            oldItem.spreadState = getSpreadState(oldItem.spread, it.spread ?: "")

                            newItem.odds.let { odds -> oldItem.odds = odds ?: 0.0 }
                            newItem.hkOdds.let { hkOdds -> oldItem.hkOdds = hkOdds ?: 0.0 }
                            newItem.spread.let { spread -> oldItem.spread = spread ?: "" }
                        }

                        newItem.status.let { status -> oldItem.status = status }
                        oldItem.betAddError = betAddError
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun getBetApi(
        betAddRequest: BetAddRequest
    ): BetAddResult? {
        //冠軍的投注要使用不同的api
        //20210824確認 都使用相同api
        return doNetwork(androidContext) {
            OneBoSportApi.betService.addBet(betAddRequest)
        }
    }

    private fun afterBet(matchType: MatchType?, result: BetAddResult?) {
        if (matchType != MatchType.PARLAY) {
            result?.receipt?.let { receipt ->
                removeBetInfoItem(receipt.singleBets?.firstOrNull()?.matchOdds?.firstOrNull()?.oddsId)
            }
        } else {
            betInfoRepository.clear()
        }
    }
}
