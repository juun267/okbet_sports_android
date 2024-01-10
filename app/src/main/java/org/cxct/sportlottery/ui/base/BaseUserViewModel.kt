package org.cxct.sportlottery.ui.base

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cn.jpush.android.api.JPushInterface
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddState
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.enums.SpreadState
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.add.BetAddRequest
import org.cxct.sportlottery.network.bet.add.Stake
import org.cxct.sportlottery.network.bet.add.betReceipt.BetAddResult
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.bet.settledDetailList.BetInfo
import org.cxct.sportlottery.network.bet.settledDetailList.BetInfoRequest
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.error.BetAddError
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.detail.CateDetailData
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.BetPlayCateFunction.isEndScoreType
import org.cxct.sportlottery.util.MatchOddUtil.applyDiscount
import org.cxct.sportlottery.util.MatchOddUtil.applyHKDiscount
import org.cxct.sportlottery.util.MatchOddUtil.setupOddsDiscount
import org.cxct.sportlottery.util.MatchOddUtil.updateDiscount
import timber.log.Timber
import java.math.RoundingMode


abstract class BaseOddButtonViewModel(
    androidContext: Application,
    loginRepository: LoginRepository,
    userInfoRepository: UserInfoRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
) : BaseWithdrawViewModel(
    androidContext,
    loginRepository,
    betInfoRepository,
    infoCenterRepository
) {

    val userInfo: LiveData<UserInfo?> = userInfoRepository.userInfo

    val showBetInfoSingle = betInfoRepository.showBetInfoSingle

    val betInfoList = betInfoRepository.betInfoList

    val betIDList = betInfoRepository.betIDList

    val showOddsChangeWarn get() = betInfoRepository.showOddsChangeWarn

    val showOddsCloseWarn get() = betInfoRepository.showOddsCloseWarn

    val hasBetPlatClose get() = betInfoRepository.hasBetPlatClose

    val oddsType: LiveData<OddsType> = MultiLanguagesApplication.mInstance.mOddsType

    val betAddResult: LiveData<Event<BetAddResult?>>
        get() = _betAddResult


    protected val mLockMoney = MutableLiveData<Double?>()
    protected val _betFailed = MutableLiveData<Pair<Boolean, String?>>()

    val userMoney: LiveData<Double?> //使用者餘額
        get() = LoginRepository.userMoney

    val lockMoney: LiveData<Double?>
        get() = mLockMoney
    val betFailed: LiveData<Pair<Boolean, String?>>
        get() = _betFailed

    private val _betAddResult = MutableLiveData<Event<BetAddResult?>>()

    val betParlaySuccess: LiveData<Boolean>
        get() = betInfoRepository.betParlaySuccess

    private val deviceId by lazy {
        JPushInterface.getRegistrationID(androidContext)
    }

    fun getMoneyAndTransferOut(allTransferOut: Boolean = true) {
        viewModelScope.launch { LoginRepository.getMoneyAndTransferOut(allTransferOut) }
    }

    fun getLockMoney() {
        viewModelScope.launch {
            val userMoneyResult = doNetwork(androidContext) {
                OneBoSportApi.userService.lockMoney()
            }
            mLockMoney.postValue(userMoneyResult?.money)
        }
    }

    fun saveOddsType(oddsType: OddsType) {
        MultiLanguagesApplication.saveOddsType(oddsType)
    }

    var savedOddId = "savedOddId"
    fun updateMatchBetListData(data: FastBetDataBean) {
        val oddId = data.odd.id
//        Timber.e("oddId: $oddId")
        if (getMarketSwitch()) return
        if (savedOddId == oddId) return
        savedOddId = oddId.orEmpty()
        if (data.matchType == MatchType.OUTRIGHT) {
            //冠军
            updateMatchBetListForOutRight(
                matchType = MatchType.OUTRIGHT,
                gameType = data.gameType,
                playCateCode = data.playCateCode ?: "",
                playCateName = data.playCateName ?: "",
                matchOdd = data.matchOdd!!,
                odd = data.odd
            )
        } else {
            //除冠军外 其他分类
            updateMatchBetList(
                data.matchType,
                data.gameType,
                data.playCateCode ?: "",
                data.playCateName ?: "",
                data.matchInfo,
                data.odd,
                data.subscribeChannelType,
                data.betPlayCateNameMap,
                data.playCateMenuCode
            )
        }
    }

//    var matchId: String? = null

    private fun updateMatchBetList(
        matchType: MatchType,
        gameType: GameType,
        playCateCode: String,
        playCateName: String,
        matchInfo: MatchInfo,
        odd: org.cxct.sportlottery.network.odds.Odd,
        subscribeChannelType: ChannelType,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        playCateMenuCode: String? = null,
        otherPlayCateName: String? = null
    ) {
        val betItem = betInfoRepository.betInfoList.value?.peekContent()
            ?.find { it.matchOdd.oddsId == odd.id }

        var currentOddsType = MultiLanguagesApplication.mInstance.mOddsType.value ?: OddsType.HK
        if (odd.odds == odd.malayOdds) {
            currentOddsType = OddsType.EU
        }

        if (betItem != null) {
            odd.id?.let { removeBetInfoItem(it) }
            return
        }

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.betService.getBetInfo(
                    BetInfoRequest(
                        matchInfo.id, odd.id.toString()
                    )
                )
            } ?: return@launch

            //如有其他地方呼叫getBetInfo，api回傳之後也要重設savedOddId
            savedOddId = "savedOddId" //重設savedOddId
            if (!result.success) {
                return@launch
            }

            val betInfo = result.BetInfo
            Timber.d("betInfoRepository:$betInfoRepository  ${betInfoRepository.currentState}")
            betInfoRepository.addInBetInfo(
                matchType,
                gameType,
                playCateCode,
                otherPlayCateName ?: playCateName,
                odd.nameMap?.get(LanguageManager.getSelectLanguage(androidContext).key)
                    ?: odd.name ?: "",
                matchInfo,
                odd,
                subscribeChannelType,
                playCateMenuCode,
                currentOddsType,
                betPlayCateNameMap,
                betInfo = betInfo
            )
        }
    }

    private fun updateMatchBetListForOutRight(
        matchType: MatchType,
        gameType: GameType,
        playCateCode: String,
        playCateName: String,
        matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd,
        odd: org.cxct.sportlottery.network.odds.Odd
    ) {

        val betItem = betInfoRepository.betInfoList.value?.peekContent()
            ?.find { it.matchOdd.oddsId == odd.id }

        var currentOddsType = MultiLanguagesApplication.mInstance.mOddsType.value
        if (odd.odds == odd.malayOdds) {
            currentOddsType = OddsType.EU
        }

        if (betItem == null) {
            viewModelScope.launch {
                doNetwork(androidContext) {
                    OneBoSportApi.betService.getBetInfo(
                        BetInfoRequest(
                            matchOdd.matchInfo?.id.toString(), odd.id.toString()
                        )
                    )
                }?.let { result ->
                    //如有其他地方呼叫getBetInfo，api回傳之後也要重設savedOddId
                    savedOddId = "savedOddId" //重設savedOddId
                    Timber.d("savedOddId result:${result}")

                    if (result.success) {
                        val betInfo = result.BetInfo
                        extracted(
                            matchOdd,
                            matchType,
                            gameType,
                            playCateCode,
                            playCateName,
                            odd,
                            currentOddsType,
                            betInfo
                        )
                    }
                }
            }
        } else {
            odd.id?.let { removeBetInfoItem(it) }
        }
    }

    private fun extracted(
        matchOdd: org.cxct.sportlottery.network.outright.odds.MatchOdd,
        matchType: MatchType,
        gameType: GameType,
        playCateCode: String,
        playCateName: String,
        odd: org.cxct.sportlottery.network.odds.Odd,
        currentOddsType: OddsType?,
        betInfo: BetInfo?
    ) {
        matchOdd.matchInfo?.let {
            betInfoRepository.addInBetInfo(
                matchType = matchType,
                gameType = gameType,
                playCateCode = playCateCode,
                playCateName = playCateName,
                playName = odd.nameMap?.get(
                    LanguageManager.getSelectLanguage(
                        androidContext
                    ).key
                ) ?: odd.name ?: "",
                matchInfo = it,
                odd = odd,
                subscribeChannelType = ChannelType.HALL,
                oddsType = currentOddsType,
                betPlayCateNameMap = matchOdd.betPlayCateNameMap,
                betInfo = betInfo
            )
        }
    }

    fun updateLockMatchOdd(matchOddsLock: FrontWsEvent.MatchOddsLockEvent) {
        betInfoRepository.betInfoList.value?.peekContent()
            ?.find { it.matchOdd.matchId == matchOddsLock.matchId }?.matchOdd?.status =
            BetStatus.LOCKED.code

        betInfoRepository.notifyBetInfoChanged()
    }

    fun updateMatchOdd(changeEvent: Any) {
        val newList: MutableList<org.cxct.sportlottery.network.odds.Odd> = mutableListOf()
        when (changeEvent) {
            is OddsChangeEvent -> {
                changeEvent.odds.forEach { map ->
                    val value = map.value
                    value?.forEach { odd ->
                        odd.let {
                            val newOdd = org.cxct.sportlottery.network.odds.Odd(
                                extInfoMap = null,
                                id = odd.id,
                                name = null,
                                originalOdds = odd.originalOdds,
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

    /**
     * 新的投注單沒有單一下注, 一次下注一整單, 下注完後不管成功失敗皆清除所有投注單內容
     * 依照 tabPosition 區分單注or串關 (0:單注, 1:串關)
     * @date 20220607
     */

    fun addBetList(
        normalBetList: List<BetInfoListData>,
        parlayBetList: List<ParlayOdd>,
        oddsType: OddsType,
        tabPosition: Int,
        oddsChangeOption: Int = 1,
    ) {
        var currentOddsTypes: OddsType
        //一般注單
        val matchList: MutableList<Odd> = mutableListOf()
        normalBetList.forEachIndexed { index, it ->
            currentOddsTypes =
                if (it.matchOdd.isOnlyEUType || it.matchType == MatchType.OUTRIGHT || it.matchType == MatchType.OTHER_OUTRIGHT || it.matchType == MatchType.END_SCORE) {
                    OddsType.EU
                } else {
                    oddsType
                }
            val betAmount = when (tabPosition) {
                0, 2 -> {
                    it.betAmount
                }

                else -> {
                    0.0
                }
            }

            if (tabPosition == 2) {
                if (index == 0) {
                    matchList.add(
                        Odd(
                            it.matchOdd.oddsId,
                            getOdds(it.matchOdd, currentOddsTypes),
                            betAmount,
                            currentOddsTypes.code
                        )
                    )
                } else {
                    matchList.add(
                        Odd(
                            it.matchOdd.oddsId, null, null, null
                        )
                    )
                }
            } else {
                matchList.add(
                    Odd(
                        it.matchOdd.oddsId,
                        getOdds(it.matchOdd, currentOddsTypes)
                            .toBigDecimal()
                            .setScale(
                                if (it.matchOdd.playCode.contains(PlayCate.LCS.value)) 4 else 2,
                                RoundingMode.HALF_UP
                            )
                            .toDouble()
                        ,
                        betAmount,
                        currentOddsTypes.code
                    )
                )
            }

        }

        //若有串關 則改為EU
        currentOddsTypes = if (normalBetList.size == 1) {
            normalBetList.getOrNull(0)?.singleBetOddsType ?: OddsType.EU
        } else {
            OddsType.EU
        }
        //串關注單
        val parlayList: MutableList<Stake> = mutableListOf()
        parlayBetList.forEach {
            if (it.betAmount > 0) {
                val betAmount = if (tabPosition == 1) it.betAmount else 0.0
                parlayList.add(
                    Stake(
                        TextUtil.replaceCByParlay(androidContext, it.parlayType), betAmount
                    )
                )
            }
        }

        val betType = if (normalBetList[0].matchOdd.playCode.isEndScoreType()) {
            1
        } else {
            0
        }

        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.betService.addBet(
                    BetAddRequest(
                        matchList,
                        parlayList,
                        oddsChangeOption,
                        2,
                        deviceId,
                        channelType = 0,//先寫死固定帶0
                        betType = betType
                    )
                )
            } ?: return@launch

            val singleBets = result.receipt?.singleBets
            singleBets?.forEach { s ->
                s.matchOdds?.forEach { m ->
                    s.matchType = normalBetList.find { betInfoListData ->
                        betInfoListData.matchOdd.oddsId == m.oddsId
                    }?.matchType
//                    s.oddsType = oddsType
                }
            }

            if (!result.success) {
                result.success = false
                _betAddResult.postValue(Event(result))
                return@launch
            }

            //检查是否有item注单下注失败
            val haveSingleItemFailed = singleBets?.any { singleIt -> singleIt.status == 7 } ?: false
            val parlayBets = result.receipt?.parlayBets
            val haveParlayItemFailed = parlayBets?.any { parlayIt -> parlayIt.status == 7 } ?: false
            val gameType = normalBetList.firstOrNull()?.matchOdd?.gameType
            if (gameType==GameType.ES.key){
                val categoryCodeList = normalBetList.groupBy { it.matchOdd.categoryCode }.keys.toList()
                categoryCodeList.forEach {
                    RecentDataManager.addRecent(RecentRecord(0,gameType = gameType,categoryCode = it))
                }
            }else{
                RecentDataManager.addRecent(RecentRecord(0,gameType = normalBetList.first().matchOdd.gameType))
            }

            if (!haveSingleItemFailed && !haveParlayItemFailed) {
                betInfoRepository.clear()
                _betFailed.postValue(Pair(false, ""))
                _betAddResult.postValue(Event(result))
                return@launch
            }

            var failedReason: String? = ""
            singleBets?.forEach {
                if (it.status != 7) {
                    it.matchOdds?.forEach {
                        betInfoRepository.removeItem(it.oddsId)
                    }
                } else {
                    failedReason = it.code
                }
            }
            parlayBets?.forEach {
                if (it.status != 7) {
                    it.matchOdds?.forEach {
                        betInfoRepository.removeItem(it.oddsId)
                    }
                } else {
                    failedReason = it.code
                }
            }
            withContext(Dispatchers.Main) {
                SingleToast.showSingleToastNoImage(
                    androidContext,
                    BetsFailedReasonUtil.getFailedReasonByCode(failedReason)
                )
            }

            result.success = false
            _betAddResult.postValue(Event(result))
            //处理赔率更新
            _betFailed.postValue(Pair(true, failedReason))
        }
    }

    fun saveOddsHasChanged(matchOdd: org.cxct.sportlottery.network.bet.info.MatchOdd) {
        betInfoRepository.saveOddsHasChanged(matchOdd)
    }

    fun removeBetInfoItem(oddId: String?) {
        savedOddId = "savedOddId" //重設savedOddId
        betInfoRepository.removeItem(oddId)
    }

    fun removeBetInfoSingle() {
        if (betInfoRepository.showBetInfoSingle.value?.peekContent() == true) betInfoRepository.clear()
    }

    fun removeClosedPlatBetInfo() {
        betInfoRepository.removeClosedPlatItem()
    }

    fun removeBetInfoAll() {
        betInfoRepository.clear()
    }

    protected fun getOddState(
        oldItemOdds: Double, newOdd: org.cxct.sportlottery.network.odds.Odd
    ): Int {
        //馬來盤、印尼盤為null時自行計算
        var newMalayOdds = 0.0
        var newIndoOdds = 0.0

        newOdd.hkOdds?.let {
            newMalayOdds =
                if (newOdd.hkOdds ?: 0.0 > 1) ArithUtil.oddIdfFormat(-1 / newOdd.hkOdds!!)
                    .toDouble() else newOdd.hkOdds ?: 0.0
            newIndoOdds = if (newOdd.hkOdds ?: 0.0 < 1) ArithUtil.oddIdfFormat(-1 / newOdd.hkOdds!!)
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

    protected fun Map<String, List<org.cxct.sportlottery.network.odds.Odd>?>.toMutableFormat(): MutableMap<String, MutableList<org.cxct.sportlottery.network.odds.Odd>?> {
        return this.mapValues { map: Map.Entry<String, List<org.cxct.sportlottery.network.odds.Odd>?> ->
            map.value?.toMutableList() ?: mutableListOf()
        }.toMutableMap()
    }

    protected fun Map<String, List<org.cxct.sportlottery.network.odds.Odd?>?>.toMutableFormat_1(): MutableMap<String, MutableList<org.cxct.sportlottery.network.odds.Odd?>?> {
        return this.mapValues { map ->
            map.value?.toMutableList() ?: mutableListOf()
        }.toMutableMap()
    }

    protected fun MatchOdd.setupOddDiscount() {
        val discount = userInfo.value?.discount ?: 1F
        this.oddsMap?.forEach {
            it.value?.filterNotNull()?.forEach { odd ->
                if (it.key == PlayCate.EPS.value) odd.setupEPSDiscount(discount)
                else odd.setupDiscount(discount)
            }
        }

        this.oddsEps?.eps?.filterNotNull()?.forEach { odd ->
            odd.setupEPSDiscount(discount)
        }

        this.quickPlayCateList?.forEach { quickPlayCate ->
            quickPlayCate.quickOdds.forEach {
                it.value?.filterNotNull()?.forEach { odd ->
                    if (it.key == PlayCate.EPS.value) odd.setupEPSDiscount(discount)
                    else odd.setupDiscount(discount)
                }
            }
        }
    }

    private fun org.cxct.sportlottery.network.odds.Odd.setupDiscount(discount: Float) {
        this.odds = this.odds?.applyDiscount(discount)
        this.hkOdds = this.hkOdds?.applyHKDiscount(discount)
    }

    protected fun org.cxct.sportlottery.network.odds.Odd.setupEPSDiscount(discount: Float) {
        this.setupDiscount(discount)
        this.extInfo = this.extInfo?.toDouble()?.applyDiscount(discount)?.toString()
    }

    protected fun MatchOdd.updateOddStatus() {
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

    protected fun org.cxct.sportlottery.network.odds.detail.MatchOdd.setupOddDiscount() {
        val discount = userInfo.value?.discount ?: 1F
        this.odds.forEach { (key, value) ->
            value.odds.filterNotNull()?.forEach { odd ->
                odd.setupOddsDiscount(key == PlayCate.LCS.value, key, discount)
            }
        }
    }

    protected fun MutableMap<String, CateDetailData>.sortPlayCate() {
        val sorted = this.toList().sortedBy { (_, value) -> value.rowSort }.toMap()
        this.clear()
        this.putAll(sorted)
    }

    private fun getSpreadState(oldSpread: String, newSpread: String): Int = when {
        newSpread != oldSpread -> SpreadState.DIFFERENT
        else -> SpreadState.SAME
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
                        var currentOddsType =
                            MultiLanguagesApplication.mInstance.mOddsType.value ?: OddsType.HK
                        if (it.odds == it.malayOdds) currentOddsType = OddsType.EU
                        if (oldItem.status == BetStatus.ACTIVATED.code) {
                            oldItem.oddState = getOddState(
                                getOdds(
                                    oldItem, currentOddsType
                                ), newItem
                            )

                            if (oldItem.oddState != OddState.SAME.state) oldItem.oddsHasChanged =
                                true
                        }

                        oldItem.spreadState = getSpreadState(oldItem.spread, it.spread ?: "")

                        if (oldItem.status == BetStatus.ACTIVATED.code) {
                            newItem.odds.let { odds -> oldItem.odds = odds ?: 0.0 }
                            newItem.hkOdds.let { hkOdds -> oldItem.hkOdds = hkOdds ?: 0.0 }
                            newItem.indoOdds.let { indoOdds -> oldItem.indoOdds = indoOdds ?: 0.0 }
                            newItem.malayOdds.let { malayOdds ->
                                oldItem.malayOdds = malayOdds ?: 0.0
                            }
                            newItem.spread.let { spread -> oldItem.spread = spread ?: "" }
                        }

                        //從socket獲取後 賠率有變動並且投注狀態開啟時 需隱藏錯誤訊息
                        if (oldItem.oddState != OddState.SAME.state && oldItem.status == BetStatus.ACTIVATED.code) {
                            oldItem.betAddError = null
                        }

                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
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
                    var currentOddsType = MultiLanguagesApplication.mInstance.mOddsType.value
                    if (it.odds == it.malayOdds) {
                        currentOddsType = OddsType.EU
                    }

                    if (it.id == oldItem.oddsId) {
                        if (betAddError == BetAddError.ODDS_HAVE_CHANGED) {
                            oldItem.oddState = getOddState(
                                getOdds(
                                    oldItem, currentOddsType ?: OddsType.HK
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

    //更新投注限額
    fun updateBetLimit() {
        betInfoList.value?.peekContent()?.forEach { betInfoListData ->
            viewModelScope.launch {
                val result = doNetwork(androidContext) {
                    OneBoSportApi.betService.getBetInfo(
                        BetInfoRequest(
                            betInfoListData.matchOdd.matchId, betInfoListData.matchOdd.oddsId
                        )
                    )
                }
                //如有其他地方呼叫getBetInfo，api回傳之後也要重設savedOddId
                savedOddId = "savedOddId" //重設savedOddId
                result?.let {
                    if (result.success) {
                        betInfoListData.betInfo = it.BetInfo
                    } else {
                        //避免登入後socket更新有時間差，當取不到限額資訊的betInfoData改為LOCKED
                        betInfoListData.matchOdd.status = BetStatus.LOCKED.code
                        betInfoListData.amountError = true
                    }
                }
            }
        }
        betInfoRepository.notifyBetInfoChanged()
    }

    fun updateBetInfoDiscount(discount: Float, newDiscount: Float) {
        betInfoList.value?.peekContent()?.forEach { betInfoListData ->
            betInfoListData.matchOdd.updateDiscount(discount, newDiscount)
        }

        betInfoRepository.notifyBetInfoChanged()
    }

}


