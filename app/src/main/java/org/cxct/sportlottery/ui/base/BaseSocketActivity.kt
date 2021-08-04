package org.cxct.sportlottery.ui.base

import android.app.ActivityManager
import android.content.*
import android.os.Bundle
import android.os.IBinder
import androidx.lifecycle.Observer
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.global_stop.GlobalStopEvent
import org.cxct.sportlottery.network.service.league_change.LeagueChangeEvent
import org.cxct.sportlottery.network.service.match_clock.MatchClockEvent
import org.cxct.sportlottery.network.service.match_odds_change.MatchOddsChangeEvent
import org.cxct.sportlottery.network.service.match_status_change.MatchStatusChangeEvent
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.service.producer_up.ProducerUpEvent
import org.cxct.sportlottery.service.BackService
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import timber.log.Timber
import java.lang.Exception
import kotlin.reflect.KClass

abstract class BaseSocketActivity<T : BaseSocketViewModel>(clazz: KClass<T>) :
    BaseFavoriteActivity<T>(clazz) {

    interface ReceiverChannelHall {
        fun onMatchStatusChanged(matchStatusChangeEvent: MatchStatusChangeEvent)
        fun onMatchClockChanged(matchClockEvent: MatchClockEvent)
        fun onOddsChanged(oddsChangeEvent: OddsChangeEvent)
        fun onLeagueChanged(leagueChangeEvent: LeagueChangeEvent)
    }

    interface ReceiverChannelEvent {
        fun onMatchOddsChanged(matchOddsChangeEvent: MatchOddsChangeEvent)
    }

    interface ReceiverChannelPublic {
        fun onGlobalStop(globalStopEvent: GlobalStopEvent)
        fun onProducerUp(producerUpEvent: ProducerUpEvent)
    }

    private var receiverChannelHall: ReceiverChannelHall? = null
    private var receiverChannelEvent: ReceiverChannelEvent? = null
    private var receiverChannelPublic: ReceiverChannelPublic? = null

    private val receiver by lazy {
        ServiceBroadcastReceiver()
    }

    private lateinit var backService: BackService

    private var isServiceBound = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Timber.e(">>> onServiceConnected")
            val binder = service as BackService.MyBinder //透過Binder調用Service內的方法
            backService = binder.service

            binder.connect(
                viewModel.loginRepository.token,
                viewModel.loginRepository.userId,
                viewModel.loginRepository.platformId
            )

            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Timber.e(">>> onServiceDisconnected")
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        receiver.sysMaintenance.observe(this, Observer {
            startActivity(Intent(this, MaintenanceActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
        })

        receiver.serviceConnectStatus.observe(this, Observer { status ->
            when (status) {
                ServiceConnectStatus.RECONNECT_FREQUENCY_LIMIT -> {
                    hideLoading()
                    showErrorPromptDialog(getString(R.string.message_socket_connect)) { backService.doReconnect() }
                }
                else -> {
                    //do nothing
                }
            }
        })

        receiver.playQuotaChange.observe(this, {
            it?.playQuotaComData?.let { playQuotaComData ->
                viewModel.updatePlayQuota(playQuotaComData)
            }
        })

        receiver.userMoney.observe(this, {
            viewModel.updateMoney(it)
        })

        receiver.orderSettlement.observe(this, {
            viewModel.getSettlementNotification(it)
        })

        receiver.userNotice.observe(this, Observer {
            it?.userNoticeList?.let { list ->
                viewModel.setUserNoticeList(list)
            }
        })

        receiver.matchStatusChange.observe(this, {
            it?.let { matchStatusChangeEvent ->
                receiverChannelHall?.onMatchStatusChanged(matchStatusChangeEvent)
            }
        })

        receiver.matchClock.observe(this, {
            it?.let { matchClockEvent ->
                receiverChannelHall?.onMatchClockChanged(matchClockEvent)
            }
        })

        receiver.oddsChange.observe(this, {
            it?.let { oddsChangeEvent ->
                receiverChannelHall?.onOddsChanged(oddsChangeEvent.updateOddsSelectedState())
            }
        })

        receiver.leagueChange.observe(this, {
            it?.let { leagueChangeEvent ->
                receiverChannelHall?.onLeagueChanged(leagueChangeEvent)
            }
        })

        receiver.matchOddsChange.observe(this, {
            it?.let { matchOddsChangeEvent ->
                receiverChannelEvent?.onMatchOddsChanged(matchOddsChangeEvent)
            }
        })

        receiver.globalStop.observe(this, {
            it?.let { globalStopEvent ->
                receiverChannelPublic?.onGlobalStop(globalStopEvent)
            }
        })

        receiver.producerUp.observe(this, {
            it?.let { producerUpEvent ->
                receiverChannelPublic?.onProducerUp(producerUpEvent)
            }
        })
    }

    fun registerChannelHall(receiverChannelHall: ReceiverChannelHall) {
        this.receiverChannelHall = receiverChannelHall
    }

    fun registerChannelEvent(receiverChannelEvent: ReceiverChannelEvent) {
        this.receiverChannelEvent = receiverChannelEvent
    }

    fun registerChannelPublic(receiverChannelPublic: ReceiverChannelPublic) {
        this.receiverChannelPublic = receiverChannelPublic
    }

    fun subscribeChannelHall(
        gameType: String?,
        cateMenuCode: String?,
        eventId: String?
    ) {
        if (receiverChannelHall == null) throw Exception("You must register receiverChannelHall interface before call fun subscribeChannelHall")
        if (receiverChannelPublic == null) throw Exception("You must register receiverChannelPublic interface before call fun subscribeChannelHall")

        backService.subscribeHallChannel(gameType, cateMenuCode, eventId)
    }

    fun subscribeChannelEvent(
        eventId: String?
    ) {
        if (receiverChannelEvent == null) throw Exception("You must register receiverChannelEvent interface before call fun subscribeChannelEvent")
        if (receiverChannelPublic == null) throw Exception("You must register receiverChannelPublic interface before call fun subscribeChannelEvent")

        backService.subscribeEventChannel(eventId)
    }

    fun unSubscribeChannelHall(
        gameType: String?,
        cateMenuCode: String?,
        eventId: String?
    ) {
        backService.unsubscribeHallChannel(gameType, cateMenuCode, eventId)
    }

    fun unSubscribeChannelEvent(eventId: String?) {
        backService.unsubscribeEventChannel(eventId)
    }

    fun unSubscribeChannelHallAll() {
        backService.unsubscribeAllHallChannel()
    }

    fun unSubscribeChannelEventAll() {
        backService.unsubscribeAllEventChannel()
    }

    override fun onStart() {
        super.onStart()

        subscribeBroadCastReceiver()
        bindService()
    }

    override fun onStop() {
        super.onStop()

        removeBroadCastReceiver()
        unBindService()
    }

    private fun bindService() {
        if (isServiceBound) return

        val serviceIntent = Intent(this, BackService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        isServiceBound = true
    }

    private fun unBindService() {
        if (!isServiceBound) return

        unbindService(serviceConnection)
        isServiceBound = false
    }

    private fun checkServiceRunning(): Boolean {
        val manager: ActivityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager

        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (BackService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun subscribeBroadCastReceiver() {
        val filter = IntentFilter().apply {
            addAction(BackService.SERVICE_SEND_DATA)
        }

        registerReceiver(receiver, filter)
    }

    private fun removeBroadCastReceiver() {
        unregisterReceiver(receiver)
    }

    private fun OddsChangeEvent.updateOddsSelectedState(): OddsChangeEvent {
        this.odds?.let { oddTypeSocketMap ->
            oddTypeSocketMap.mapValues { oddTypeSocketMapEntry ->
                oddTypeSocketMapEntry.value.onEach { odd ->
                    odd?.isSelected =
                        viewModel.betInfoList.value?.peekContent()?.any { betInfoListData ->
                            betInfoListData.matchOdd.oddsId == odd?.id
                        }
                }
            }
        }

        return this
    }
}