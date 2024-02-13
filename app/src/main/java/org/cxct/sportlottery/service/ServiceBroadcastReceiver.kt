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
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.post
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.match_odds_change.transferMatchOddsChangeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.service.odds_change.transferOddsChangeEvent
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.SocketUpdateUtil.replaceNameMap
import timber.log.Timber
import java.math.BigDecimal
import java.sql.Timestamp

object ServiceBroadcastReceiver {

    private val betInfoRepository: BetInfoRepository = BetInfoRepository

    val globalStop: LiveData<FrontWsEvent.GlobalStopEvent?>
        get() = _globalStop

    val matchClock: LiveData<FrontWsEvent.MatchClockEvent?>
        get() = _matchClock

    val notice: LiveData<FrontWsEvent.NoticeEvent?>
        get() = _notice

    val orderSettlement: LiveData<FrontWsEvent.BetSettlementEvent?>
        get() = _orderSettlement

    val producerUp: LiveData<FrontWsEvent.ProducerUpEvent?>
        get() = _producerUp

    val userMoney: LiveData<Double?>
        get() = _userMoney

    val lockMoney: LiveData<Double?>
        get() = _lockMoney

    val userNotice: LiveData<Event<FrontWsEvent.UserNoticeEvent>>
        get() = _userNotice

    val sysMaintenance: LiveData<FrontWsEvent.SysMaintainEvent?>
        get() = _sysMaintenance

    val serviceConnectStatus: LiveData<ServiceConnectStatus>
        get() = _serviceConnectStatus

    val leagueChange: LiveData<FrontWsEvent.LeagueChangeEvent?>
        get() = _leagueChange

    val matchOddsLock = SocketRepository.matchOddsLock

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

    //在后台超过一段时间后，返回前台刷新赛事列表
    val refreshInForeground: LiveData<Event<Long>>
        get() = _refreshInForeground

    private val _globalStop = MutableLiveData<FrontWsEvent.GlobalStopEvent?>()
    private val _matchClock = MutableLiveData<FrontWsEvent.MatchClockEvent?>()
    private val _notice = MutableLiveData<FrontWsEvent.NoticeEvent?>()
    private val _orderSettlement = MutableLiveData<FrontWsEvent.BetSettlementEvent?>()
    private val _pingPong = MutableLiveData<FrontWsEvent.PingPongEvent?>()
    private val _producerUp = MutableLiveData<FrontWsEvent.ProducerUpEvent?>()
    private val _userMoney = MutableLiveData<Double?>()
    private val _lockMoney = MutableLiveData<Double?>()
    private val _userNotice = MutableLiveData<Event<FrontWsEvent.UserNoticeEvent>>()
    private val _sysMaintenance = SingleLiveEvent<FrontWsEvent.SysMaintainEvent?>()
    private val _serviceConnectStatus = SingleLiveEvent<ServiceConnectStatus>()
    private val _leagueChange = MutableLiveData<FrontWsEvent.LeagueChangeEvent?>()
    private val _userDiscountChange = MutableLiveData<FrontWsEvent.UserDiscountChangeEvent?>()
    private val _userMaxBetMoneyChange = MutableLiveData<FrontWsEvent.UserLevelConfigChangeEvent?>()
    private val _dataSourceChange = MutableLiveData<Boolean?>()
    private val _userInfoChange = MutableLiveData<Boolean?>()
    private val _closePlayCate = MutableLiveData<Event<FrontWsEvent.ClosePlayCateEvent?>>()
    private val _refreshInForeground = MutableLiveData<Event<Long>>()

    val sportMaintenance: LiveData<FrontWsEvent.SportMaintainEvent> = MutableLiveData()
    val jackpotChange: LiveData<String?> = MutableLiveData()
    val onSystemStatusChange: LiveData<Boolean> = SingleLiveEvent()

    val thirdGamesMaintain = MutableSharedFlow<FrontWsEvent.GameFirmMaintainEvent>(extraBufferCapacity= 3)

    fun onConnectStatus(connectStatus: ServiceConnectStatus) {
        _serviceConnectStatus.postValue(connectStatus)
    }

    fun onReceiveMessage(channelStr: String, messageStr: String,gameType: String? = null) {

        CoroutineScope(Dispatchers.IO).launch {

            try {
                val eventsList = EncryptUtil.uncompressProto(messageStr)?.eventsList ?: return@launch
                if (eventsList.isNotEmpty()) {
                    eventsList.forEach { event ->
                        handleEvent(event, channelStr,gameType)
                    }
                }

            } catch (e: Exception) {
                Timber.e("JSONException WS格式出問題 $messageStr")
                e.printStackTrace()
            }
        }
    }

    private suspend fun handleEvent(event: FrontWsEvent.Event, channelStr: String,gameType: String? = null) {
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
                event.userNoticeEvent?.let { _userNotice.postValue(Event(it)) }
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
                SocketRepository.emitMatchOddsLock(event.matchOddsLockEvent)
            }
            //賠率折扣
            EventType.USER_DISCOUNT_CHANGE -> {
                _userDiscountChange.postValue(event.userDiscountChangeEvent)
                event.userDiscountChangeEvent.discountByGameTypeListList?.let { discount ->
                    betInfoRepository.updateDiscount(discount)
                }
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
            //賽事狀態
            EventType.MATCH_STATUS_CHANGE -> {
                post { MatchOddsRepository.onMatchStatus(event.matchStatusChangeEvent) }
            }
            //大廳賠率
            EventType.ODDS_CHANGE -> {
                val data = event.oddsChangeEvent.transferOddsChangeEvent()
                data.channel = channelStr

                data.oddsList.forEach { // 过滤掉空odd(2023.05.30)
                    if (it.oddsListList != null) {
                        val iterator = it.oddsListList.iterator()
                        while (iterator.hasNext()) {
                            if (iterator.next() == null) {
                                iterator.remove()
                            }
                        }
                    }
                }

                // 登陆的用户计算赔率折扣
                if (LoginRepository.isLogined()) {
                    val discount = MultiLanguagesApplication.mInstance.userInfo()?.getDiscount(data.gameType)?.toBigDecimalOrNull() ?: BigDecimal.ONE
                    if (discount != BigDecimal.ONE) {
                        data.setupOddDiscount(discount)
                    }
                }
                data.updateOddsSelectedState()
                data.sortOddsMap()
                data.apply {
                    playCateNameMap.addSplitPlayCateTranslation()
                    betPlayCateNameMap.addSplitPlayCateTranslation()

                    val newPlayCateNameMap = playCateNameMap
                    val newBetPlayCateNameMap = betPlayCateNameMap
                    playCateNameMap = replaceTargetNameMap(newPlayCateNameMap)
                    betPlayCateNameMap = replaceTargetNameMap(newBetPlayCateNameMap)
                }
                onOddsEvent(data)

            }
            //詳情頁賠率
            EventType.MATCH_ODDS_CHANGE -> {
                val data = event.matchOddsChangeEvent.transferMatchOddsChangeEvent()
                // 登陆的用户计算赔率折扣
                if (LoginRepository.isLogined()) {
                    val discount =
                        MultiLanguagesApplication.mInstance.userInfo()?.getDiscount(gameType)?.toBigDecimalOrNull()
                            ?: BigDecimal.ONE
                    if (discount != BigDecimal.ONE) {
                        data.setupOddDiscount(discount)
                    }
                }
                data.updateOddsSelectedState()
//                data.sortOddsMap()
                post{
                    MatchOddsRepository.onMatchOdds(data)
                }
                BetInfoRepository.updateMatchOdd(data)
            }
            //体育服务开关
            EventType.SPORT_MAINTAIN_STATUS -> {
                (sportMaintenance as MutableLiveData<FrontWsEvent.SportMaintainEvent>)
                    .postValue(event.sportMaintainEvent)
            }

            EventType.ORDER_SETTLEMENT -> {
                _orderSettlement.postValue(event.betSettlementEvent)
            }

            else -> {
                Timber.i("Receive UnKnown EventType : $eventType")
            }
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

    //SINGLE_OU、SINGLE_BTS兩種玩法要特殊處理，後端API沒給翻譯
    private fun MutableMap<String?, MutableMap<String?, String?>?>?.addSplitPlayCateTranslation(): MutableMap<String?, Map<String?, String?>?> {
        val translationMap = mutableMapOf<String?, Map<String?, String?>?>()

        this?.let { translationMap.putAll(it) }

        val ou_o_Map: MutableMap<String, String> = mutableMapOf()
        val ou_u_Map: MutableMap<String, String> = mutableMapOf()
        val bts_y_Map: MutableMap<String, String> = mutableMapOf()
        val bts_n_Map: MutableMap<String, String> = mutableMapOf()
        for (language in LanguageManager.Language.values()) {
            ou_o_Map[language.key] = LocalUtils.getString(R.string.J801)
            ou_u_Map[language.key] = LocalUtils.getString(R.string.J802)
            bts_y_Map[language.key] = LocalUtils.getString(R.string.J803)
            bts_n_Map[language.key] = LocalUtils.getString(R.string.J804)
        }
        translationMap[PlayCate.SINGLE_OU_O.value] = ou_o_Map.toMap()
        translationMap[PlayCate.SINGLE_OU_U.value] = ou_u_Map.toMap()
        translationMap[PlayCate.SINGLE_BTS_Y.value] = bts_y_Map.toMap()
        translationMap[PlayCate.SINGLE_BTS_N.value] = bts_n_Map.toMap()
        return translationMap
    }

    private fun OddsChangeEvent.replaceTargetNameMap(targetNameMap: MutableMap<String?, MutableMap<String?, String?>?>):
            MutableMap<String?, MutableMap<String?, String?>?> {
        val oddsMap = this.odds
        oddsMap.forEach { odds ->
            odds.value?.toList()?.forEach { odd ->
                val extInfo = if (odd.extInfo.isNullOrEmpty()) "{E}" else odd.extInfo ?: "{E}"
                targetNameMap.toMap().forEach { (playCode, values) ->
                    val oddsKey = when {
                        odds.key.contains(":") -> odds.key.split(":").firstOrNull()
                        else -> odds.key
                    }
                    if (oddsKey == playCode) {
                        values?.toList()?.forEach { (localCode, translateName) ->
                            val replacedName = translateName?.replace("{E}", extInfo)
                            values[localCode] = replacedName
                        }
                        targetNameMap[playCode] = values
                    }
                }
            }
        }
        return targetNameMap
    }

    private fun OddsChangeEvent.setupOddDiscount(discount: BigDecimal): OddsChangeEvent {
        this.odds.let { oddsMap ->
            oddsMap.forEach { (key, value) ->
                if (key != PlayCate.LCS.value) {
                    value?.forEach { odd ->
                        odd.updateDiscount(discount)
                    }
                }
            }
        }

        return this
    }

    private fun MatchOddsChangeEvent.setupOddDiscount(discount: BigDecimal): MatchOddsChangeEvent {
        this.odds?.let { oddsMap ->
            oddsMap.forEach { (key, value) ->
                if (!key.contains(PlayCate.LCS.value)) {
                    value.odds?.forEach { odd ->
                        odd?.updateDiscount(discount)
                    }
                }
            }
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
        if (lifecycleOwner is BaseFragment<*,*> && !lifecycleOwner.isVisibleToUser()) {
            return
        }
        // 如果某个页面需要订阅赔率变化，发现赛事订阅了但是赔率没变化此时可以从这里排查。是不是回调监听被其他地方抢注了
//        Log.e("For Test", "========>>> ServiceBroadcastReceiver ${lifecycleOwner.javaClass.name}")
        oddsChangeListener = listener
    }

    class OddsChangeListener(val onOddsChangeListener: (oddsChangeEvent: OddsChangeEvent) -> Unit)

    fun postRefrehInForeground(interval: Long){
        _refreshInForeground.postValue(Event(interval))
    }
}

