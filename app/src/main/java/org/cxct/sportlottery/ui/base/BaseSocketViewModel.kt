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
import org.cxct.sportlottery.common.enums.*
import org.cxct.sportlottery.network.NetResult
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.add.BetAddRequest
import org.cxct.sportlottery.network.bet.add.Stake
import org.cxct.sportlottery.network.bet.add.betReceipt.BetAddResult
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.bet.settledDetailList.BetInfo
import org.cxct.sportlottery.network.bet.settledDetailList.BetInfoRequest
import org.cxct.sportlottery.network.common.*
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.detail.CateDetailData
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.service.order_settlement.Status
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.network.withdraw.uwcheck.ValidateTwoFactorRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.service.BackService
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.BetPlayCateFunction.isEndScoreType
import org.cxct.sportlottery.util.MatchOddUtil.applyDiscount
import org.cxct.sportlottery.util.MatchOddUtil.applyHKDiscount
import org.cxct.sportlottery.util.MatchOddUtil.setupOddsDiscount
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode


abstract class BaseSocketViewModel(
    androidContext: Application
) : BaseViewModel(
    androidContext
) {

    val notifyLogin: SingleLiveEvent<Boolean>
        get() = mNotifyLogin
    val mNotifyLogin = SingleLiveEvent<Boolean>()

    val isLogin: LiveData<Boolean>
        get() = LoginRepository.isLogin

    val isKickedOut: LiveData<Event<String?>> by lazy {
        LoginRepository.kickedOut
    }
    val notifyMyFavorite = FavoriteRepository.favorNotify
    val detailNotifyMyFavorite = FavoriteRepository.detailFavorNotify

    val favorSportList = FavoriteRepository.favorSportList

    val favorLeagueList = FavoriteRepository.favorLeagueList

    val favorMatchList: LiveData<Set<String>> = FavoriteRepository.favorMatchList

    val favorPlayCateList = FavoriteRepository.favorPlayCateList

    val favoriteOutrightList = FavoriteRepository.favoriteOutrightList

    val settlementNotificationMsg
        get() = BetInfoRepository.settlementNotificationMsg
    val userInfo: LiveData<UserInfo?> = UserInfoRepository.userInfo

    val showBetInfoSingle = BetInfoRepository.showBetInfoSingle

    val betInfoList = BetInfoRepository.betInfoList

    val betIDList = BetInfoRepository.betIDList

    val showOddsChangeWarn get() = BetInfoRepository.showOddsChangeWarn

    val showOddsCloseWarn get() = BetInfoRepository.showOddsCloseWarn

    val hasBetPlatClose get() = BetInfoRepository.hasBetPlatClose

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
        get() = BetInfoRepository.betParlaySuccess

    private val deviceId by lazy {
        JPushInterface.getRegistrationID(androidContext)
    }
    val withdrawRepository = WithdrawRepository

    val withdrawSystemOperation = withdrawRepository.withdrawSystemOperation

    //提款頁面是否需要更新提款密碼 true: 需要, false: 不需要
    val needToUpdateWithdrawPassword = withdrawRepository.needToUpdateWithdrawPassword

    //提款設置頁面是否需要更新提款密碼 true: 需要, false: 不需要
    val settingNeedToUpdateWithdrawPassword = withdrawRepository.settingNeedToUpdateWithdrawPassword

    //提款設置頁面是否需要完善個人資料 true: 需要, false: 不需要
    val settingNeedToCompleteProfileInfo = withdrawRepository.settingNeedToCompleteProfileInfo

    //提款頁面是否需要完善個人資料 true: 需要, false: 不需要
    val needToCompleteProfileInfo = withdrawRepository.needToCompleteProfileInfo

    //提款頁面是否需要新增銀行卡 -1 : 不需要新增, else : 以value作為string id 顯示彈窗提示
    val needToBindBankCard = withdrawRepository.needToBindBankCard

    //判斷是不是要進行手機驗證 true: 需要, false: 不需要
    val needToSendTwoFactor = withdrawRepository.showSecurityDialog

    //進入提款頁前判斷
    val intoWithdraw = withdrawRepository.intoWithdraw

    //需要完善個人資訊(缺電話號碼) needPhoneNumber
    val showPhoneNumberMessageDialog = withdrawRepository.hasPhoneNumber

    //發送簡訊碼之後60s無法再發送
    val twoFactorResult: LiveData<NetResult?>
        get() = _twoFactorResult
    private val _twoFactorResult = MutableLiveData<NetResult?>()

    //錯誤提示
    val errorMessageDialog: LiveData<String?>
        get() = _errorMessageDialog
    private val _errorMessageDialog = MutableLiveData<String?>()

    //認證成功
    val twoFactorSuccess: LiveData<Boolean?>
        get() = _twoFactorSuccess
    private val _twoFactorSuccess = MutableLiveData<Boolean?>()

    //是否正在请求充值开关
    private var checkRecharge = false

    //是否正在请求提现开关
    private var checkWithdraw = false

    //判斷提現是否需要KYC認證
    private var _isWithdrawShowVerifyDialog = SingleLiveEvent<Event<Boolean>>()
    val isWithdrawShowVerifyDialog: LiveData<Event<Boolean>>
        get() = _isWithdrawShowVerifyDialog

    //判斷充值是否需要KYC認證
    private var _isRechargeShowVerifyDialog = SingleLiveEvent<Event<Boolean>>()
    val isRechargeShowVerifyDialog: LiveData<Event<Boolean>>
        get() = _isRechargeShowVerifyDialog

    fun doLogoutAPI() {
        viewModelScope.launch {
            runCatching { LoginRepository.logoutAPI() }
        }
    }
    fun doCleanToken() {
        viewModelScope.launch {
            BetInfoRepository.clear()
            InfoCenterRepository.clear()
            LoginRepository.logout()
            BackService.cleanUserChannel()
        }
    }

    fun doLogoutCleanUser(finishFunction: () -> Unit) {
        viewModelScope.launch {
            BetInfoRepository.clear()
            InfoCenterRepository.clear()
            LoginRepository.logout()
            //退出登入後盤口回到預設
            updateDefaultHandicapType()
            finishFunction.invoke()
        }
    }

    fun checkIsUserAlive() {
        viewModelScope.launch {
            doNetwork(MultiLanguagesApplication.appContext) {
                LoginRepository.checkIsUserAlive()
            }.let { result ->
                if (result?.success == false && LoginRepository.isLogin.value == true) {
                    LoginRepository._kickedOut.value = Event(result.msg)
                }
            }
        }
    }

    fun getLoginBoolean(): Boolean {
        return LoginRepository.isLogin.value ?: false
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
        val betItem = BetInfoRepository.betInfoList.value?.peekContent()
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
            Timber.d("BetInfoRepository:$BetInfoRepository  ${BetInfoRepository.currentState}")
            BetInfoRepository.addInBetInfo(
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

        val betItem = BetInfoRepository.betInfoList.value?.peekContent()
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
            BetInfoRepository.addInBetInfo(
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
        BetInfoRepository.betInfoList.value?.peekContent()
            ?.find { it.matchOdd.matchId == matchOddsLock.matchId }?.matchOdd?.status =
            BetStatus.LOCKED.code

        BetInfoRepository.notifyBetInfoChanged()
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
        BetInfoRepository.betInfoList.value?.peekContent()?.forEach {
            updateItem(it.matchOdd, newList)
        }
        BetInfoRepository.notifyBetInfoChanged()

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
                            currentOddsTypes.code,
                            it.matchOdd.spread
                        )
                    )
                } else {
                    matchList.add(
                        Odd(
                            it.matchOdd.oddsId,
                            null,
                            null,
                            null,
                            it.matchOdd.spread
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
                        currentOddsTypes.code,
                        it.matchOdd.spread
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
                BetInfoRepository.clear()
                _betFailed.postValue(Pair(false, ""))
                _betAddResult.postValue(Event(result))
                return@launch
            }

            var failedReason: String? = ""
            singleBets?.forEach {
                if (it.status != 7) {
                    it.matchOdds?.forEach {
                        BetInfoRepository.removeItem(it.oddsId)
                    }
                } else {
                    failedReason = it.code
                }
            }
            parlayBets?.forEach {
                if (it.status != 7) {
                    it.matchOdds?.forEach {
                        BetInfoRepository.removeItem(it.oddsId)
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
        BetInfoRepository.saveOddsHasChanged(matchOdd)
    }

    fun removeBetInfoItem(oddId: String?) {
        savedOddId = "savedOddId" //重設savedOddId
        BetInfoRepository.removeItem(oddId)
    }

    fun removeBetInfoSingle() {
        if (BetInfoRepository.showBetInfoSingle.value?.peekContent() == true) BetInfoRepository.clear()
    }

    fun removeClosedPlatBetInfo() {
        BetInfoRepository.removeClosedPlatItem()
    }

    fun removeBetInfoAll() {
        BetInfoRepository.clear()
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
        val discount = userInfo.value?.getDiscount(this.matchInfo?.gameType)?.toBigDecimalOrNull()?.toFloat() ?: BigDecimal.ONE.toFloat()
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
        val discount = userInfo.value?.getDiscount(matchInfo.gameType)?.toBigDecimalOrNull() ?: BigDecimal.ONE
        this.odds.forEach { (key, value) ->
            value.odds.filterNotNull()?.forEach { odd ->
                odd.setupOddsDiscount(key == PlayCate.LCS.value, key, discount.toFloat())
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
        BetInfoRepository.notifyBetInfoChanged()
    }


    //提款功能是否啟用
    fun checkWithdrawSystem() {
        if (checkWithdraw)
            return
        viewModelScope.launch {
            checkWithdraw = true
            doNetwork(androidContext) {
                withdrawRepository.checkWithdrawSystem()
            }
            checkWithdraw = false
        }
    }

    private var _rechargeSystemOperation = MutableLiveData<Event<Boolean>>()
    val rechargeSystemOperation: LiveData<Event<Boolean>>
        get() = _rechargeSystemOperation

    //充值功能是否啟用
    fun checkRechargeSystem() {
        if (checkRecharge)
            return
        viewModelScope.launch {
            checkRecharge = true
            val result = doNetwork(androidContext) {
                withdrawRepository.checkRechargeSystem()
            }

            if (result == null || !result.success) return@launch

            val rechTypesList = result.rechCfg?.rechTypes //玩家層級擁有的充值方式
            val rechCfgsList = result.rechCfg?.rechCfgs  //後台有開的充值方式
            val operation = (rechTypesList?.size ?: 0 > 0) && (rechCfgsList?.size ?: 0 > 0)
            _rechargeSystemOperation.value = Event(operation)

            checkRecharge = false
        }
    }

    fun checkWithdrawKYCVerify(){
        _isWithdrawShowVerifyDialog.postValue(Event(UserInfoRepository.userInfo.value?.verified != VerifiedType.PASSED.value && isKYCVerifyWithdrawOpen()))
    }

    fun checkRechargeKYCVerify(){
        _isRechargeShowVerifyDialog.postValue(Event(UserInfoRepository.userInfo.value?.verified != VerifiedType.PASSED.value && isKYCVerifyRechargeOpen()))
    }

    /**
     * 判斷個人資訊是否完整, 若不完整需要前往個人資訊頁面完善資料.
     * complete true: 個人資訊有缺漏, false: 個人資訊完整
     */
    fun checkProfileInfoComplete() {
        viewModelScope.launch {
            withdrawRepository.checkProfileInfoComplete()
        }
    }

    fun checkBankCardPermissions() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                withdrawRepository.checkBankCardPermissions()
            }
        }
    }

    //發送簡訊驗證碼
    fun sendTwoFactor() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.withdrawService.sendTwoFactor()
            }
            _twoFactorResult.postValue(result)
        }
    }

    //双重验证校验
    fun validateTwoFactor(validateTwoFactorRequest: ValidateTwoFactorRequest) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.withdrawService.validateTwoFactor(validateTwoFactorRequest)
            }?.let { result ->
                if(result.success){
                    _twoFactorSuccess.value = true
                    withdrawRepository.sendTwoFactor()
                }
                else
                    _errorMessageDialog.value = result.msg
            }
        }
    }
    fun updateMoney(money: Double?) {
        LoginRepository.updateMoney(money)
    }

    fun updateLockMoney(money: Double?) {
        mLockMoney.postValue(money)
    }

    fun getSettlementNotification(event: FrontWsEvent.BetSettlementEvent?) {
        event?.sportBet?.let {
            when (it.status) {
                Status.UN_CHECK.code, Status.UN_DONE.code, Status.WIN.code, Status.WIN_HALF.code, Status.CANCEL.code,  Status.LOSE.code,  Status.LOSE_HALF.code,  Status.DRAW.code -> {
                    BetInfoRepository.postSettlementNotificationMsg(it)
                }
            }
        }
    }

    fun updateDiscount(discountByGameTypeList: List<FrontWsEvent.DiscountByGameTypeVO>?) {
        viewModelScope.launch {
            if (discountByGameTypeList.isNullOrEmpty()) {
                viewModelScope.launch {
                    doNetwork { UserInfoRepository.getUserInfo() }
                }
            } else {
                userInfo.value?.userId?.let { UserInfoRepository.updateDiscount(discountByGameTypeList) }
            }
        }
    }
    fun getFavorite() {
        if (isLogin.value != true) {
            mNotifyLogin.postValue(true)
            return
        }
        doRequest({ FavoriteRepository.getFavorite() }) { }
    }


    fun clearFavorite() {
        FavoriteRepository.clearFavorite()
    }

    fun notifyFavorite(type: FavoriteType) {
        FavoriteRepository.notifyFavorite(type)
    }

    fun pinFavorite(
        type: FavoriteType, content: String?, gameType: String? = null
    ) {
        if (!LoginRepository.isLogined()) {
            mNotifyLogin.postValue(true)
            return
        }

        doRequest({ FavoriteRepository.pinFavorite(type, content, gameType) }) {

        }
    }

    /**
     * 檢查當前登入狀態, 若未登入則跳請登入提示
     * @return true: 已登入, false: 未登入
     */
    open fun checkLoginStatus(): Boolean {
        return if (isLogin.value != true) {
            mNotifyLogin.postValue(true)
            false
        } else {
            true
        }
    }
}


