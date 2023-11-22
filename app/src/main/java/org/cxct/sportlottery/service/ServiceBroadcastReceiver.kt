package org.cxct.sportlottery.service


import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.common.extentions.post
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.service.order_settlement.OrderSettlementEvent
import org.cxct.sportlottery.network.service.sys_maintenance.SportMaintenanceEvent
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.MatchOddUtil.applyDiscount
import org.cxct.sportlottery.util.MatchOddUtil.applyHKDiscount
import org.cxct.sportlottery.util.MatchOddUtil.convertToIndoOdds
import org.cxct.sportlottery.util.MatchOddUtil.convertToMYOdds
import org.cxct.sportlottery.util.MatchOddUtil.setupOddsDiscount
import org.cxct.sportlottery.util.OddsUtil.updateBetStatus
import org.cxct.sportlottery.util.OddsUtil.updateBetStatus_1
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import timber.log.Timber

object ServiceBroadcastReceiver {

    val globalStop: LiveData<FrontWsEvent.GlobalStopEvent?>
        get() = _globalStop

    val matchClock: LiveData<FrontWsEvent.MatchClockEvent?>
        get() = _matchClock

    val notice: LiveData<FrontWsEvent.NoticeEvent?>
        get() = _notice

    val orderSettlement: LiveData<OrderSettlementEvent?>
        get() = _orderSettlement

    val producerUp: LiveData<FrontWsEvent.ProducerUpEvent?>
        get() = _producerUp

    val userMoney: LiveData<Double?>
        get() = _userMoney

    val lockMoney: LiveData<Double?>
        get() = _lockMoney

    val userNotice: LiveData<FrontWsEvent.UserNoticeEvent?>
        get() = _userNotice

    val sysMaintenance: LiveData<FrontWsEvent.SysMaintainEvent?>
        get() = _sysMaintenance

    val serviceConnectStatus: LiveData<ServiceConnectStatus>
        get() = _serviceConnectStatus

    val leagueChange: LiveData<FrontWsEvent.LeagueChangeEvent?>
        get() = _leagueChange

    val matchOddsLock: LiveData<FrontWsEvent.MatchOddsLockEvent?>
        get() = _matchOddsLock

    val userDiscountChange: LiveData<FrontWsEvent.UserDiscountChangeEvent?>
        get() = _userDiscountChange

    val userMaxBetMoneyChange: LiveData<FrontWsEvent.UserLevelConfigChangeEvent?>
        get() = _userMaxBetMoneyChange

    val dataSourceChange: LiveData<Boolean?>
        get() = _dataSourceChange

    val userInfoChange: LiveData<Boolean?>
        get() = _userInfoChange

    val closePlayCate: LiveData<Event<FrontWsEvent.ClosePlayCateEvent?>>
        get() = _closePlayCate

    private val _globalStop = MutableLiveData<FrontWsEvent.GlobalStopEvent?>()
    private val _matchClock = MutableLiveData<FrontWsEvent.MatchClockEvent?>()
    private val _notice = MutableLiveData<FrontWsEvent.NoticeEvent?>()
    private val _orderSettlement = MutableLiveData<OrderSettlementEvent?>()
    private val _pingPong = MutableLiveData<FrontWsEvent.PingPongEvent?>()
    private val _producerUp = MutableLiveData<FrontWsEvent.ProducerUpEvent?>()
    private val _userMoney = MutableLiveData<Double?>()
    private val _lockMoney = MutableLiveData<Double?>()
    private val _userNotice = MutableLiveData<FrontWsEvent.UserNoticeEvent?>()
    private val _sysMaintenance = SingleLiveEvent<FrontWsEvent.SysMaintainEvent?>()
    private val _serviceConnectStatus = SingleLiveEvent<ServiceConnectStatus>()
    private val _leagueChange = MutableLiveData<FrontWsEvent.LeagueChangeEvent?>()
    private val _matchOddsLock = MutableLiveData<FrontWsEvent.MatchOddsLockEvent?>()
    private val _userDiscountChange = MutableLiveData<FrontWsEvent.UserDiscountChangeEvent?>()
    private val _userMaxBetMoneyChange = MutableLiveData<FrontWsEvent.UserLevelConfigChangeEvent?>()
    private val _dataSourceChange = MutableLiveData<Boolean?>()
    private val _userInfoChange = MutableLiveData<Boolean?>()
    private val _closePlayCate = MutableLiveData<Event<FrontWsEvent.ClosePlayCateEvent?>>()

    val sportMaintenance: LiveData<SportMaintenanceEvent> = MutableLiveData()
    val jackpotChange: LiveData<String?> = MutableLiveData()
    val onSystemStatusChange: LiveData<Boolean> = SingleLiveEvent()

    val thirdGamesMaintain = MutableSharedFlow<FrontWsEvent.GameFirmMaintainEvent>(extraBufferCapacity= 3)


    fun onConnectStatus(connectStatus: ServiceConnectStatus) {
        _serviceConnectStatus.postValue(connectStatus)
    }

    fun onReceiveMessage(channelStr: String, messageStr: String) {

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val decryptMessage = EncryptUtil.uncompress(messageStr)
                if (decryptMessage.isNullOrEmpty()) {
                    return@launch
                }

                val json = JSONTokener(decryptMessage).nextValue()
                if (json is JSONArray) {
                    var jsonArray = JSONArray(decryptMessage)
                    for (i in 0 until jsonArray.length()) {
                        var jObj = jsonArray.optJSONObject(i)
                        val jObjStr = jObj.toString()
                        handleEvent(jObj, jObjStr, channelStr)
                    }
                } else if (json is JSONObject) {
                    val jObjStr = json.toString()
                    handleEvent(json, jObjStr, channelStr)
                }

//                //TODO: 全部格式轉換完畢後，替換為 uncompressProto
//                val decryptProtoMessage = EncryptUtil.uncompressProto(messageStr) ?: return@launch
//                decryptProtoMessage.let {
//                    if (it.eventsList.isNotEmpty()) {
//                        it.eventsList.forEach { event ->
//                            handleEvent(event, channelStr)
//                        }
//                    }
//                }

            } catch (e: Exception) {
                Timber.e("JSONException WS格式出問題 $messageStr")
                e.printStackTrace()
            }
        }
    }

    private suspend fun handleEvent(event: FrontWsEvent.Event, channelStr: String) {
        when (val eventType = event.eventType) {
            EventType.NOTICE -> {
                _notice.postValue(event.noticeEvent)
            }
            EventType.GLOBAL_STOP -> {
                _globalStop.postValue(event.globalStopEvent)
            }
            EventType.PRODUCER_UP -> {
                _producerUp.postValue(event.producerUpEvent)
            }
            //公共频道(这个通道会通知主站平台维护)
            EventType.SYS_MAINTENANCE -> {
                val sysMaintainEvent = event.sysMaintainEvent
                _sysMaintenance.postValue(sysMaintainEvent)
                (onSystemStatusChange as MutableLiveData<Boolean>).postValue(sysMaintainEvent.status == 1)
            }
            EventType.RECORD_RESULT_JACKPOT_OK_GAMES->{
                val data = event.recordResultJackpotOkGamesEvent
                (jackpotChange as MutableLiveData<String?>).postValue(data?.amount)
            }
            //公共频道
            EventType.DATA_SOURCE_CHANGE -> {
                _dataSourceChange.postValue(true)
            }
            EventType.CLOSE_PLAY_CATE -> {
                _closePlayCate.postValue(Event(event.closePlayCateEvent))
            }
            //用户私人频道
            EventType.USER_MONEY -> {
                _userMoney.postValue(event.userMoneyEvent.money.toDoubleOrNull())
            }
            EventType.LOCK_MONEY -> {
                _lockMoney.postValue(event.userLockMoneyEvent.lockMoney.toDoubleOrNull())
            }
            EventType.USER_NOTICE -> {
                _userNotice.postValue(event.userNoticeEvent)
            }
            EventType.PING_PONG -> {
                _pingPong.postValue(event.pingPongEvent)
            }
            EventType.MATCH_CLOCK -> {
                _matchClock.postValue(event.matchClockEvent)
            }
            EventType.LEAGUE_CHANGE -> {
                _leagueChange.postValue(event.leagueChangeEvent)
            }
            EventType.MATCH_ODDS_LOCK -> {
                _matchOddsLock.postValue(event.matchOddsLockEvent)
            }
            //賠率折扣
            EventType.USER_DISCOUNT_CHANGE -> {
                _userDiscountChange.postValue(event.userDiscountChangeEvent)
            }
            //特定VIP层级的最新设定内容(會影響最大下注金額)
            EventType.USER_LEVEL_CONFIG_CHANGE -> {
                _userMaxBetMoneyChange.postValue(event.userLevelConfigChangeEvent)
            }
            //用戶資訊成功
            EventType.USER_INFO_CHANGE -> {
                _userInfoChange.postValue(true)
            }
            EventType.THIRD_GAME_STATU_CHANGED -> { // 三方游戏维护状态
                thirdGamesMaintain.emit(event.gameFirmMaintainEvent)
            }

            else -> {
                Timber.i("Receive UnKnown EventType : $eventType")
            }
        }
    }

    private suspend fun handleEvent(jObj: JSONObject, jObjStr: String, channelStr: String) {
        when (val eventType = jObj.optString("eventType")) {
            //体育服务开关
            EventType.SPORT_MAINTAIN_STATUS -> {
                ServiceMessage.getSportMaintenance(jObjStr)?.let {
                    (sportMaintenance as MutableLiveData<SportMaintenanceEvent>).postValue(it)
                }
                //TODO: proto 缺 SPORT_MAINTAIN_STATUS
            }
            EventType.ORDER_SETTLEMENT -> {
                val data = ServiceMessage.getOrderSettlement(jObjStr)
                _orderSettlement.postValue(data)
                //TODO: proto 缺 BetSettlementEvent.BaseSportBet.cancelReason
            }

            //大廳賠率
            EventType.MATCH_STATUS_CHANGE -> {
                ServiceMessage.getMatchStatusChange(jObjStr)?.let {
                    post { MatchOddsRepository.onMatchStatus(it) }
                }
                //TODO: proto 缺 statusNameI18n 需從 /api/front/index/resource.json 取得更新翻譯
            }
            EventType.ODDS_CHANGE -> {
                val data = ServiceMessage.getOddsChange(jObjStr) ?: return
                data.channel = channelStr

                data.oddsList.forEach { // 过滤掉空odd(2023.05.30)
                    if (it.oddsList != null) {
                        val iterator = it.oddsList?.iterator()
                        while (iterator.hasNext()) {
                            if (iterator.next() == null) {
                                iterator.remove()
                            }
                        }
                    }
                }

                // 登陆的用户计算赔率折扣
                if (LoginRepository.isLogined()) {
                    data.setupOddDiscount(UserInfoRepository.getDiscount())
                }
                data.oddsListToOddsMap()
                data.updateOddsSelectedState()
                data.sortOddsMap()
                onOddsEvent(data)

            }
            //具体赛事/赛季频道
            EventType.MATCH_ODDS_CHANGE -> {
                val data = ServiceMessage.getMatchOddsChange(jObjStr)?:return
                // 登陆的用户计算赔率折扣
                if (LoginRepository.isLogined()) {
                    data.setupOddDiscount(UserInfoRepository.getDiscount())
                }
                data.updateOddsSelectedState()
                post{
                    MatchOddsRepository.onMatchOdds(data)
                }
                BetInfoRepository.updateMatchOdd(data)
            }
            else -> {  }

        }

    }

    private suspend fun onOddsEvent(socketEvent: OddsChangeEvent) {
        oddsChangeListener?.let {
            withContext(Dispatchers.Main) {
                it.onOddsChangeListener(socketEvent)
            }
        }
        BetInfoRepository.updateMatchOdd(socketEvent)
    }
    private fun OddsChangeEvent.oddsListToOddsMap() {
        odds = oddsList.associateBy(
            keySelector = { it.playCateCode.toString() },
            valueTransform = { it.oddsList?.filter { it != null }?.toMutableList() }).toMutableMap()
    }
    private fun OddsChangeEvent.setupOddDiscount(discount: Float): OddsChangeEvent {
        if (1.0f == discount) {
            return this
        }
        this.oddsList.let { oddTypeSocketList ->
            oddTypeSocketList.forEach { oddsList ->
                if (oddsList.playCateCode == PlayCate.LCS.value) {
                    oddsList.oddsList?.setupOddsDiscount(true,
                        oddsList.playCateCode,
                        discount)
                } else {
                    oddsList.oddsList?.setupOddsDiscount(false,
                        oddsList.playCateCode,
                        discount)
                }
            }
        }

        return this
    }
    private fun List<Odd?>.setupOddsDiscount(
        isLCS: Boolean,
        playCateCode: String?,
        discount: Float,
    ) {
        this.forEach { odd ->
            odd.setupOddsDiscount(isLCS, playCateCode, discount)
        }
        this?.toMutableList().updateBetStatus()
    }
    /**
     * 得在所有賠率折扣率, 水位計算完畢後再去判斷更新盤口狀態
     */
    private fun MatchOddsChangeEvent.updateBetStatus(): MatchOddsChangeEvent {
        this.odds?.let { oddsMap ->
            oddsMap.forEach { (_, value) ->
                value.odds?.updateBetStatus()
            }
        }
        return this
    }

    private fun MatchOddsChangeEvent.setupOddDiscount(discount: Float): MatchOddsChangeEvent {
        if (discount == 1.0f) {
            return this
        }
        this.odds?.let { oddsMap ->
            oddsMap.forEach { (key, value) ->
                if (!key.contains(PlayCate.LCS.value)) {//反波膽不處理折扣
                    value.odds?.forEach { odd ->
                        odd?.odds = odd?.odds?.applyDiscount(discount)
                        odd?.hkOdds = odd?.hkOdds?.applyHKDiscount(discount)
                        odd?.malayOdds = odd?.hkOdds?.convertToMYOdds()
                        odd?.indoOdds = odd?.hkOdds?.convertToIndoOdds()

                        if (key == PlayCate.EPS.value) {
                            odd?.extInfo =
                                odd?.extInfo?.toDouble()?.applyDiscount(discount)?.toString()
                        }
                    }
                }
            }
        }
        return this
    }
    /**
     * 得在所有賠率折扣率, 水位計算完畢後再去判斷更新盤口狀態
     */
    private fun OddsChangeEvent.updateBetStatus(): OddsChangeEvent {
        oddsList.forEach { oddsList ->
            oddsList.oddsList?.updateBetStatus_1()
        }
        return this
    }
    private fun OddsChangeEvent.sortOddsMap() {
        this.odds.sortOddsMap()
    }

    private fun OddsChangeEvent.updateOddsSelectedState(): OddsChangeEvent {
        this.odds.let { oddTypeSocketMap ->
            oddTypeSocketMap.mapValues { oddTypeSocketMapEntry ->
                oddTypeSocketMapEntry.value?.onEach { odd ->
                    odd?.isSelected =
                        BetInfoRepository.betInfoList.value?.peekContent()?.any { betInfoListData ->
                            betInfoListData.matchOdd?.oddsId == odd?.id
                        } == true
                }
            }
        }

        return this
    }

    private fun MatchOddsChangeEvent.updateOddsSelectedState(): MatchOddsChangeEvent {
        this.odds?.let { oddTypeSocketMap ->
            oddTypeSocketMap.mapValues { oddTypeSocketMapEntry ->
                oddTypeSocketMapEntry.value.odds?.onEach { odd ->
                    odd?.isSelected =
                        BetInfoRepository.betInfoList.value?.peekContent()?.any { betInfoListData ->
                            betInfoListData.matchOdd.oddsId == odd?.id
                        } == true
                }
            }
        }

        return this
    }

    var oddsChangeListener: OddsChangeListener? = null
    private set

    fun addOddsChangeListener(lifecycleOwner: LifecycleOwner, listener: OddsChangeListener) {
        if (lifecycleOwner is BaseFragment<*> && !lifecycleOwner.isVisibleToUser()) {
            return
        }
        // 如果某个页面需要订阅赔率变化，发现赛事订阅了但是赔率没变化此时可以从这里排查。是不是回调监听被其他地方抢注了
//        Log.e("For Test", "========>>> ServiceBroadcastReceiver ${lifecycleOwner.javaClass.name}")
        oddsChangeListener = listener
    }

    class OddsChangeListener(val onOddsChangeListener: (oddsChangeEvent: OddsChangeEvent) -> Unit)
}
