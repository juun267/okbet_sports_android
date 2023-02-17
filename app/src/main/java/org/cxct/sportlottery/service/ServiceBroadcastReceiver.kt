package org.cxct.sportlottery.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.common.SelectionType
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.UserDiscountChangeEvent
import org.cxct.sportlottery.network.service.close_play_cate.ClosePlayCateEvent
import org.cxct.sportlottery.network.service.global_stop.GlobalStopEvent
import org.cxct.sportlottery.network.service.league_change.LeagueChangeEvent
import org.cxct.sportlottery.network.service.match_clock.MatchClockEvent
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.match_odds_lock.MatchOddsLockEvent
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusChangeEvent
import org.cxct.sportlottery.network.service.notice.NoticeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.service.order_settlement.OrderSettlementEvent
import org.cxct.sportlottery.network.service.ping_pong.PingPongEvent
import org.cxct.sportlottery.network.service.producer_up.ProducerUpEvent
import org.cxct.sportlottery.network.service.sys_maintenance.SysMaintenanceEvent
import org.cxct.sportlottery.network.service.user_level_config_change.UserLevelConfigListEvent
import org.cxct.sportlottery.network.service.user_notice.UserNoticeEvent
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.PlayRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.service.BackService.Companion.CHANNEL_KEY
import org.cxct.sportlottery.service.BackService.Companion.CONNECT_STATUS
import org.cxct.sportlottery.service.BackService.Companion.SERVER_MESSAGE_KEY
import org.cxct.sportlottery.service.BackService.Companion.mUserId
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.MatchOddUtil.applyDiscount
import org.cxct.sportlottery.util.MatchOddUtil.applyHKDiscount
import org.cxct.sportlottery.util.MatchOddUtil.convertToIndoOdds
import org.cxct.sportlottery.util.MatchOddUtil.convertToMYOdds
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import timber.log.Timber

open class ServiceBroadcastReceiver(
    val userInfoRepository: UserInfoRepository? = null,
    val betInfoRepository: BetInfoRepository
) :
    BroadcastReceiver() {

    val globalStop: LiveData<GlobalStopEvent?>
        get() = _globalStop

    val matchClock: LiveData<MatchClockEvent?>
        get() = _matchClock

    val matchOddsChange: LiveData<Event<MatchOddsChangeEvent?>>
        get() = _matchOddsChange

    val matchStatusChange: LiveData<MatchStatusChangeEvent?>
        get() = _matchStatusChange

    val notice: LiveData<NoticeEvent?>
        get() = _notice

    val orderSettlement: LiveData<OrderSettlementEvent?>
        get() = _orderSettlement

    val pingPong: LiveData<PingPongEvent?>
        get() = _pingPong

    val producerUp: LiveData<ProducerUpEvent?>
        get() = _producerUp

    val userMoney: LiveData<Double?>
        get() = _userMoney

    val lockMoney: LiveData<Double?>
        get() = _lockMoney

    val userNotice: LiveData<UserNoticeEvent?>
        get() = _userNotice

    val sysMaintenance: LiveData<SysMaintenanceEvent?>
        get() = _sysMaintenance

    val serviceConnectStatus: LiveData<ServiceConnectStatus>
        get() = _serviceConnectStatus

    val leagueChange: LiveData<LeagueChangeEvent?>
        get() = _leagueChange

    val matchOddsLock: LiveData<MatchOddsLockEvent?>
        get() = _matchOddsLock

    val userDiscountChange: LiveData<UserDiscountChangeEvent?>
        get() = _userDiscountChange

    val userMaxBetMoneyChange: LiveData<UserLevelConfigListEvent?>
        get() = _userMaxBetMoneyChange

    val dataSourceChange: LiveData<Boolean?>
        get() = _dataSourceChange

    val userInfoChange: LiveData<Boolean?>
        get() = _userInfoChange

    val closePlayCate: LiveData<Event<ClosePlayCateEvent?>>
        get() = _closePlayCate

    private val _globalStop = MutableLiveData<GlobalStopEvent?>()
    private val _matchClock = MutableLiveData<MatchClockEvent?>()
    private val _matchOddsChange = MutableLiveData<Event<MatchOddsChangeEvent?>>()
    private val _matchStatusChange = MutableLiveData<MatchStatusChangeEvent?>()
    private val _notice = MutableLiveData<NoticeEvent?>()
    private val _orderSettlement = MutableLiveData<OrderSettlementEvent?>()
    private val _pingPong = MutableLiveData<PingPongEvent?>()
    private val _producerUp = MutableLiveData<ProducerUpEvent?>()
    private val _userMoney = MutableLiveData<Double?>()
    private val _lockMoney = MutableLiveData<Double?>()
    private val _userNotice = MutableLiveData<UserNoticeEvent?>()
    private val _sysMaintenance = MutableLiveData<SysMaintenanceEvent?>()
    private val _serviceConnectStatus = MutableLiveData<ServiceConnectStatus>()
    private val _leagueChange = MutableLiveData<LeagueChangeEvent?>()
    private val _matchOddsLock = MutableLiveData<MatchOddsLockEvent?>()
    private val _userDiscountChange = MutableLiveData<UserDiscountChangeEvent?>()
    private val _userMaxBetMoneyChange = MutableLiveData<UserLevelConfigListEvent?>()
    private val _dataSourceChange = MutableLiveData<Boolean?>()
    private val _userInfoChange = MutableLiveData<Boolean?>()
    private val _closePlayCate = MutableLiveData<Event<ClosePlayCateEvent?>>()

    override fun onReceive(context: Context?, intent: Intent) {
        val bundle = intent.extras
        receiveConnectStatus(bundle)
        receiveMessage(bundle)
    }

    private fun receiveConnectStatus(bundle: Bundle?) {
        val connectStatus = bundle?.get(CONNECT_STATUS) as ServiceConnectStatus?
        connectStatus?.let { status ->
            _serviceConnectStatus.postValue(status)
        }
    }

    private fun receiveMessage(bundle: Bundle?) {
        CoroutineScope(Dispatchers.IO).launch {
            val channelStr = bundle?.getString(CHANNEL_KEY, "") ?: ""
            val messageStr = bundle?.getString(SERVER_MESSAGE_KEY, "") ?: ""
            val decryptMessage = EncryptUtil.uncompress(messageStr)
            try {
                decryptMessage?.let {
                    if (it.isNotEmpty()) {
                        val json = JSONTokener(it).nextValue()
                        if (json is JSONArray) {
                            var jsonArray = JSONArray(it)
                            for (i in 0 until jsonArray.length()) {
                                var jObj = jsonArray.optJSONObject(i)
                                val jObjStr = jObj.toString()
                                handleEvent(jObj, jObjStr, channelStr)
                            }
                        } else if (json is JSONObject) {
                            val jObjStr = json.toString()
                            handleEvent(json, jObjStr, channelStr)
                        }
                    }
                }
            } catch (e: JSONException) {
                Log.e("JSONException", "WS格式出問題 $messageStr")
                e.printStackTrace()
            }
        }
    }

    private suspend fun handleEvent(jObj: JSONObject, jObjStr: String, channelStr: String) {
        when (val eventType = EventType.getEventType(jObj.optString("eventType"))) {
            EventType.NOTICE -> {
                val data = ServiceMessage.getNotice(jObjStr)
                _notice.postValue(data)
            }
            EventType.GLOBAL_STOP -> {
                val data = ServiceMessage.getGlobalStop(jObjStr)
                _globalStop.postValue(data)

            }
            EventType.PRODUCER_UP -> {
                val data = ServiceMessage.getProducerUp(jObjStr)
                _producerUp.postValue(data)
            }

            //公共频道(这个通道会通知主站平台维护)
            EventType.SYS_MAINTENANCE -> {
                val data = ServiceMessage.getSysMaintenance(jObjStr)
                _sysMaintenance.postValue(data)
            }

            //公共频道
            EventType.DATA_SOURCE_CHANGE -> {
                _dataSourceChange.postValue(true)
            }
            EventType.CLOSE_PLAY_CATE -> {
                _closePlayCate.postValue(Event(ServiceMessage.getClosePlayCate(jObjStr)))
            }

            //用户私人频道
            EventType.USER_MONEY -> {
                val data = ServiceMessage.getUserMoney(jObjStr)
                _userMoney.postValue(data?.money)
            }
            EventType.LOCK_MONEY -> {
                val data = ServiceMessage.getLockMoney(jObjStr)
                _lockMoney.postValue(data?.lockMoney)
            }
            EventType.USER_NOTICE -> {
                val data = ServiceMessage.getUserNotice(jObjStr)
                _userNotice.postValue(data)
            }
            EventType.ORDER_SETTLEMENT -> {
                val data = ServiceMessage.getOrderSettlement(jObjStr)
                _orderSettlement.postValue(data)
            }
            EventType.PING_PONG -> {
                val data = ServiceMessage.getPingPong(jObjStr)
                _pingPong.postValue(data)
            }

            //大廳賠率
            EventType.MATCH_STATUS_CHANGE -> {
                val data = ServiceMessage.getMatchStatusChange(jObjStr)
                _matchStatusChange.postValue(data)
            }
            EventType.MATCH_CLOCK -> {
                val data = ServiceMessage.getMatchClock(jObjStr)
                _matchClock.postValue(data)
            }
            EventType.ODDS_CHANGE -> {
                val data = ServiceMessage.getOddsChange(jObjStr)?.apply {
                    channel = channelStr
                }

                //query為耗時任務不能在主線程, LiveData需在主線程更新
                mUserId?.let { userId ->
                    val discount = userInfoRepository?.getDiscount(userId)
                    data?.let {
                        it.setupOddDiscount(discount ?: 1.0F)
                        SocketUpdateUtil.updateMatchOdds(it)
                        it.updateOddsSelectedState()
                        it.filterMenuPlayCate()
                        it.sortOddsMap()
                    }

                    data?.let { onOddsEvent(it) }

                } ?: run {
                    data?.let { onOddsEvent(it) }
                }
            }
            EventType.LEAGUE_CHANGE -> {
                val data = ServiceMessage.getLeagueChange(jObjStr)
                _leagueChange.postValue(data)
            }
            EventType.MATCH_ODDS_LOCK -> {
                val data = ServiceMessage.getMatchOddsLock(jObjStr)
                _matchOddsLock.postValue(data)
            }
            //具体赛事/赛季频道
            EventType.MATCH_ODDS_CHANGE -> {
                val data = ServiceMessage.getMatchOddsChange(jObjStr)
                //query為耗時任務不能在主線程, LiveData需在主線程更新
                mUserId?.let { userId ->
                    val discount = userInfoRepository?.getDiscount(userId)
                    data?.let {
                        it.setupOddDiscount(discount ?: 1.0F)
                        it.updateOddsSelectedState()
                    }
                    _matchOddsChange.postValue(Event(data))
                    data?.let { socketEvent ->
                        betInfoRepository.updateMatchOdd(socketEvent)
                    }

                } ?: run {
                    _matchOddsChange.postValue(Event(data))
                    data?.let { socketEvent ->
                        betInfoRepository.updateMatchOdd(socketEvent)
                    }
                }
            }
            //賠率折扣
            EventType.USER_DISCOUNT_CHANGE -> {
                val data = ServiceMessage.getUserDiscountChange(jObjStr)
                _userDiscountChange.postValue(data)
            }
            //特定VIP层级的最新设定内容(會影響最大下注金額)
            EventType.USER_LEVEL_CONFIG_CHANGE -> {
                val data = ServiceMessage.getUserMaxBetMoney(jObjStr)
                _userMaxBetMoneyChange.postValue(data)
            }
            //用戶資訊成功
            EventType.USER_INFO_CHANGE -> {
                _userInfoChange.postValue(true)
            }
            EventType.UNKNOWN -> {
                Timber.i("Receive UnKnown EventType : ${eventType.value}")
            }
        }

    }

    private suspend fun onOddsEvent(socketEvent: OddsChangeEvent) {
        oddsChangeListener?.let {
            withContext(Dispatchers.Main) {
                it.onChange(socketEvent)
            }
        }
        betInfoRepository.updateMatchOdd(socketEvent)
    }

    private fun OddsChangeEvent.setupOddDiscount(discount: Float): OddsChangeEvent {
        this.oddsList.let { oddTypeSocketList ->
            oddTypeSocketList.forEach { oddsList ->
                if (oddsList.playCateCode != PlayCate.LCS.value) {
                    oddsList.oddsList?.forEach { odd ->
                        if (odd != null && odd?.playCode != PlayCate.LCS.value) {
                            val oddsDiscount = odd?.odds?.applyDiscount(discount)
                            odd?.odds = odd?.odds?.applyDiscount(discount)

                            if (odd?.odds == odd?.hkOdds && odd?.odds == odd?.malayOdds && odd?.odds == odd?.indoOdds) {
                                odd?.hkOdds = oddsDiscount
                                odd?.malayOdds = oddsDiscount
                                odd?.indoOdds = oddsDiscount
                            } else {
                                odd?.hkOdds = odd?.hkOdds?.applyHKDiscount(discount)
                                odd?.malayOdds = odd?.hkOdds?.convertToMYOdds()
                                odd?.indoOdds = odd?.hkOdds?.convertToIndoOdds()
                            }

                            if (oddsList.playCateCode == PlayCate.EPS.value) {
                                odd?.extInfo =
                                    odd?.extInfo?.toDouble()?.applyDiscount(discount)?.toString()
                            }
                        }
                    }
                }
            }
        }

        return this
    }

    private fun MatchOddsChangeEvent.setupOddDiscount(discount: Float): MatchOddsChangeEvent {
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

    private fun OddsChangeEvent.sortOddsMap() {
        this.odds.sortOddsMap()
    }

    private fun OddsChangeEvent.updateOddsSelectedState(): OddsChangeEvent {
        this.odds.let { oddTypeSocketMap ->
            oddTypeSocketMap.mapValues { oddTypeSocketMapEntry ->
                oddTypeSocketMapEntry.value?.onEach { odd ->
                    odd?.isSelected =
                        betInfoRepository.betInfoList.value?.peekContent()?.any { betInfoListData ->
                            betInfoListData.matchOdd?.oddsId == odd?.id
                        }
                }
            }
        }

        return this
    }

    /**
     * 只有有下拉篩選玩法的才需要過濾odds
     */
    private fun OddsChangeEvent.filterMenuPlayCate() {
        val playSelected = PlayRepository.playList.value?.peekContent()?.find { it.isSelected }

        when (playSelected?.selectionType) {
            SelectionType.SELECTABLE.code -> {
                val playCateMenuCode = playSelected.playCateList?.find { it.isSelected }?.code
                this.odds.entries.retainAll { oddMap -> oddMap.key == playCateMenuCode }
            }
        }
    }

    private fun MatchOddsChangeEvent.updateOddsSelectedState(): MatchOddsChangeEvent {
        this.odds?.let { oddTypeSocketMap ->
            oddTypeSocketMap.mapValues { oddTypeSocketMapEntry ->
                oddTypeSocketMapEntry.value.odds?.onEach { odd ->
                    odd?.isSelected =
                        betInfoRepository.betInfoList.value?.peekContent()?.any { betInfoListData ->
                            betInfoListData.matchOdd.oddsId == odd?.id
                        }
                }
            }
        }

        return this
    }

    var oddsChangeListener: OddsChangeListener? = null

    class OddsChangeListener(
        val onOddsChangeListener: (oddsChangeEvent: OddsChangeEvent) -> Unit
    ) {
        fun onChange(oddsChangeEvent: OddsChangeEvent) = onOddsChangeListener(oddsChangeEvent)
    }
}
